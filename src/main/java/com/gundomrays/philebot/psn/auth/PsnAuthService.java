package com.gundomrays.philebot.psn.auth;

import com.gundomrays.philebot.psn.domain.PsnTokenResponse;
import com.gundomrays.philebot.telegram.data.SettingsRepository;
import com.gundomrays.philebot.telegram.domain.Settings;
import io.micrometer.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Authenticates as the PlayStation App: NPSSO cookie -> authorization code -> access and refresh tokens.
// The refresh token is kept in the settings table, so the NPSSO is only needed when it expires.
@Service
public class PsnAuthService {

    private static final Logger log = LoggerFactory.getLogger(PsnAuthService.class);

    static final String REFRESH_TOKEN_KEY = "PSN_REFRESH_TOKEN";

    static final String REFRESH_EXPIRES_AT_KEY = "PSN_REFRESH_EXPIRES_AT";

    private static final Duration EXPIRY_MARGIN = Duration.ofMinutes(1);

    private static final String NPSSO_EXPIRED_CODE = "4165";

    private static final Pattern ERROR_CODE = Pattern.compile("\"error_code\"\\s*:\\s*(\\d+)");

    private final WebClient webClient;

    private final SettingsRepository settingsRepository;

    private final Clock clock;

    private final String authUrl;

    private final String npsso;

    private final String clientId;

    private final String clientBasicAuth;

    private final String redirectUri;

    private final String scope;

    private String accessToken;

    private Instant accessTokenExpiresAt = Instant.EPOCH;

    private String refreshToken;

    private Instant refreshTokenExpiresAt = Instant.EPOCH;

    private boolean refreshTokenLoaded;

    @Autowired
    public PsnAuthService(WebClient.Builder webClientBuilder,
                          SettingsRepository settingsRepository,
                          @Value("${psn.authUrl}") String authUrl,
                          @Value("${psn.npsso}") String npsso,
                          @Value("${psn.clientId}") String clientId,
                          @Value("${psn.clientBasicAuth}") String clientBasicAuth,
                          @Value("${psn.redirectUri}") String redirectUri,
                          @Value("${psn.scope}") String scope,
                          @Value("${psn.userAgent}") String userAgent) {
        this(webClientBuilder, settingsRepository, Clock.systemUTC(),
                authUrl, npsso, clientId, clientBasicAuth, redirectUri, scope, userAgent);
    }

    PsnAuthService(WebClient.Builder webClientBuilder, SettingsRepository settingsRepository, Clock clock,
                   String authUrl, String npsso, String clientId, String clientBasicAuth,
                   String redirectUri, String scope, String userAgent) {
        this.webClient = webClientBuilder.defaultHeader(HttpHeaders.USER_AGENT, userAgent).build();
        this.settingsRepository = settingsRepository;
        this.clock = clock;
        this.authUrl = authUrl;
        this.npsso = npsso;
        this.clientId = clientId;
        this.clientBasicAuth = clientBasicAuth;
        this.redirectUri = redirectUri;
        this.scope = scope;
    }

    public synchronized String accessToken() {
        final Instant now = clock.instant();
        if (accessToken != null && now.isBefore(accessTokenExpiresAt.minus(EXPIRY_MARGIN))) {
            return accessToken;
        }

        loadRefreshToken();
        if (refreshToken != null && now.isBefore(refreshTokenExpiresAt)) {
            try {
                updateTokens(refreshTokens());
                return accessToken;
            } catch (PsnAuthenticationException e) {
                log.warn("PSN refresh token was rejected: {}. Trying NPSSO.", e.getMessage());
                refreshToken = null;
            }
        }

        if (StringUtils.isBlank(npsso)) {
            throw new PsnAuthenticationException("No valid PSN refresh token and no NPSSO configured");
        }
        updateTokens(exchangeCode(authorizationCode()));
        return accessToken;
    }

    // Called after the API answered 401, so the next request gets a fresh access token
    public synchronized void invalidateAccessToken() {
        accessToken = null;
    }

    public synchronized Instant refreshTokenExpiresAt() {
        loadRefreshToken();
        return refreshToken != null ? refreshTokenExpiresAt : null;
    }

    private String authorizationCode() {
        final URI uri = UriComponentsBuilder.fromUriString(authUrl)
                .path("/authorize")
                .queryParam("access_type", "offline")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", scope)
                .encode()
                .build()
                .toUri();

        // Sony answers with a redirect to the app scheme, the code is in the Location header
        final String location = webClient.get()
                .uri(uri)
                .header(HttpHeaders.COOKIE, "npsso=" + npsso)
                .exchangeToMono(response -> {
                    final String header = response.headers().asHttpHeaders().getFirst(HttpHeaders.LOCATION);
                    if (!response.statusCode().is3xxRedirection() || header == null) {
                        return response.releaseBody().then(Mono.error(new PsnAuthenticationException(
                                "PSN authorize returned status " + response.statusCode().value())));
                    }
                    return response.releaseBody().thenReturn(header);
                })
                .block();

        final Map<String, String> params = queryParams(location);
        if (params.containsKey("error")) {
            if (NPSSO_EXPIRED_CODE.equals(params.get("error_code"))) {
                throw new PsnAuthenticationException("PSN NPSSO is expired or invalid");
            }
            throw new PsnAuthenticationException("PSN authorize failed with error code " + params.get("error_code"));
        }
        final String code = params.get("code");
        if (StringUtils.isBlank(code)) {
            throw new PsnAuthenticationException("PSN authorize returned no code");
        }
        return code;
    }

    private PsnTokenResponse exchangeCode(final String code) {
        final MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("code", code);
        form.add("redirect_uri", redirectUri);
        form.add("grant_type", "authorization_code");
        form.add("token_format", "jwt");
        log.info("Exchanging PSN authorization code for tokens");
        return tokenRequest(form);
    }

    private PsnTokenResponse refreshTokens() {
        final MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("refresh_token", refreshToken);
        form.add("grant_type", "refresh_token");
        form.add("token_format", "jwt");
        form.add("scope", scope);
        log.info("Refreshing PSN access token");
        return tokenRequest(form);
    }

    private PsnTokenResponse tokenRequest(final MultiValueMap<String, String> form) {
        final PsnTokenResponse response = webClient.post()
                .uri(UriComponentsBuilder.fromUriString(authUrl).path("/token").build().toUri())
                .header(HttpHeaders.AUTHORIZATION, "Basic " + clientBasicAuth)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(form))
                .exchangeToMono(clientResponse -> {
                    if (clientResponse.statusCode().is2xxSuccessful()) {
                        return clientResponse.bodyToMono(PsnTokenResponse.class);
                    }
                    // The error body holds only error codes, never tokens
                    return clientResponse.bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .flatMap(body -> Mono.error(new PsnAuthenticationException(String.format(
                                    "PSN token request failed with status %d, error code %s",
                                    clientResponse.statusCode().value(), errorCode(body)))));
                })
                .block();

        if (response == null || StringUtils.isBlank(response.getAccessToken())) {
            throw new PsnAuthenticationException("PSN token response has no access token");
        }
        return response;
    }

    private void updateTokens(final PsnTokenResponse response) {
        final Instant now = clock.instant();
        accessToken = response.getAccessToken();
        accessTokenExpiresAt = now.plusSeconds(response.getExpiresIn() != null ? response.getExpiresIn() : 0L);
        if (StringUtils.isNotBlank(response.getRefreshToken())) {
            refreshToken = response.getRefreshToken();
            refreshTokenExpiresAt = now.plusSeconds(
                    response.getRefreshTokenExpiresIn() != null ? response.getRefreshTokenExpiresIn() : 0L);
            saveSetting(REFRESH_TOKEN_KEY, "PSN refresh token of the bot account", refreshToken);
            saveSetting(REFRESH_EXPIRES_AT_KEY, "PSN refresh token expiry, epoch seconds",
                    String.valueOf(refreshTokenExpiresAt.getEpochSecond()));
        }
        log.info("PSN access token valid until {}, refresh token valid until {}", accessTokenExpiresAt, refreshTokenExpiresAt);
    }

    private void loadRefreshToken() {
        if (refreshTokenLoaded) {
            return;
        }
        refreshTokenLoaded = true;
        final String token = settingValue(REFRESH_TOKEN_KEY);
        final String expiresAt = settingValue(REFRESH_EXPIRES_AT_KEY);
        if (StringUtils.isNotBlank(token) && StringUtils.isNotBlank(expiresAt)) {
            try {
                refreshTokenExpiresAt = Instant.ofEpochSecond(Long.parseLong(expiresAt.trim()));
                refreshToken = token;
                log.info("Loaded PSN refresh token, valid until {}", refreshTokenExpiresAt);
            } catch (NumberFormatException e) {
                log.warn("Stored PSN refresh token expiry is not a number, ignoring the stored token");
            }
        }
    }

    private String settingValue(final String key) {
        return settingsRepository.findById(key).map(Settings::getValue).orElse(null);
    }

    private void saveSetting(final String key, final String description, final String value) {
        final Settings setting = settingsRepository.findById(key).orElseGet(Settings::new);
        setting.setId(key);
        setting.setDescription(description);
        setting.setValue(value);
        settingsRepository.save(setting);
    }

    private static Map<String, String> queryParams(final String location) {
        final Map<String, String> params = new HashMap<>();
        final int queryStart = location.indexOf('?');
        if (queryStart < 0) {
            return params;
        }
        for (final String pair : location.substring(queryStart + 1).split("&")) {
            final int separator = pair.indexOf('=');
            if (separator > 0) {
                params.put(URLDecoder.decode(pair.substring(0, separator), StandardCharsets.UTF_8),
                        URLDecoder.decode(pair.substring(separator + 1), StandardCharsets.UTF_8));
            }
        }
        return params;
    }

    private static String errorCode(final String body) {
        final Matcher matcher = ERROR_CODE.matcher(body);
        return matcher.find() ? matcher.group(1) : "unknown";
    }

}
