package com.gundomrays.philebot.psn.api;

import com.google.common.util.concurrent.RateLimiter;
import com.gundomrays.philebot.psn.api.exception.PsnAccessDeniedException;
import com.gundomrays.philebot.psn.api.exception.PsnApiException;
import com.gundomrays.philebot.psn.api.exception.PsnUnauthorizedException;
import com.gundomrays.philebot.psn.auth.PsnAuthService;
import com.gundomrays.philebot.psn.domain.PsnProfileLookup;
import com.gundomrays.philebot.psn.domain.PsnTrophyDefinitions;
import com.gundomrays.philebot.psn.domain.PsnTrophyTitles;
import com.gundomrays.philebot.psn.domain.PsnUserTrophies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;

// Unofficial PSN trophy endpoints used by the PlayStation App, see CLAUDE.md
@Service
@SuppressWarnings("UnstableApiUsage")
public class PsnApiClient {

    private static final Logger log = LoggerFactory.getLogger(PsnApiClient.class);

    private static final String ALL_GROUPS = "all";

    private final WebClient webClient;

    private final PsnAuthService psnAuthService;

    private final RateLimiter rateLimiter;

    private final String apiUrl;

    private final String legacyProfileUrl;

    public PsnApiClient(WebClient.Builder webClientBuilder,
                        PsnAuthService psnAuthService,
                        @Value("${psn.apiUrl}") String apiUrl,
                        @Value("${psn.legacyProfileUrl}") String legacyProfileUrl,
                        @Value("${psn.userAgent}") String userAgent,
                        @Value("${psn.language}") String language,
                        @Value("${psn.requestsPerMin}") Double requestsPerMin) {
        this.webClient = webClientBuilder
                .defaultHeader(HttpHeaders.USER_AGENT, userAgent)
                .defaultHeader(HttpHeaders.ACCEPT_LANGUAGE, language)
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(5000 * 1024))
                .build();
        this.psnAuthService = psnAuthService;
        this.apiUrl = apiUrl;
        this.legacyProfileUrl = legacyProfileUrl;
        this.rateLimiter = RateLimiter.create(requestsPerMin / 60.0);
    }

    // Titles ordered by the time of the latest earned trophy, newest first
    public PsnTrophyTitles trophyTitles(final String accountId, final int limit, final int offset) {
        final URI uri = UriComponentsBuilder.fromUriString(apiUrl)
                .path("/trophy/v1/users/{accountId}/trophyTitles")
                .queryParam("limit", limit)
                .queryParam("offset", offset)
                .buildAndExpand(accountId)
                .encode()
                .toUri();
        return get(uri, PsnTrophyTitles.class);
    }

    // Earned state of every trophy in the title, without names and icons
    public PsnUserTrophies earnedTrophies(final String accountId, final String npCommunicationId, final String npServiceName) {
        final URI uri = UriComponentsBuilder.fromUriString(apiUrl)
                .path("/trophy/v1/users/{accountId}/npCommunicationIds/{npCommunicationId}/trophyGroups/{group}/trophies")
                .queryParam("npServiceName", npServiceName)
                .buildAndExpand(accountId, npCommunicationId, ALL_GROUPS)
                .encode()
                .toUri();
        return get(uri, PsnUserTrophies.class);
    }

    // Names, descriptions and icons of every trophy in the title
    public PsnTrophyDefinitions titleTrophies(final String npCommunicationId, final String npServiceName) {
        final URI uri = UriComponentsBuilder.fromUriString(apiUrl)
                .path("/trophy/v1/npCommunicationIds/{npCommunicationId}/trophyGroups/{group}/trophies")
                .queryParam("npServiceName", npServiceName)
                .buildAndExpand(npCommunicationId, ALL_GROUPS)
                .encode()
                .toUri();
        return get(uri, PsnTrophyDefinitions.class);
    }

    // Exact lookup of the account id by the PSN Online ID
    public String accountId(final String onlineId) {
        final URI uri = UriComponentsBuilder.fromUriString(legacyProfileUrl)
                .path("/{onlineId}/profile2")
                .queryParam("fields", "accountId,onlineId,currentOnlineId")
                .buildAndExpand(onlineId)
                .encode()
                .toUri();
        final PsnProfileLookup lookup = get(uri, PsnProfileLookup.class);
        if (lookup == null || lookup.getProfile() == null || lookup.getProfile().getAccountId() == null) {
            throw new PsnAccessDeniedException(HttpStatus.NOT_FOUND.value(), "No PSN account found for " + onlineId);
        }
        return lookup.getProfile().getAccountId();
    }

    private <T> T get(final URI uri, final Class<T> clazz) {
        try {
            return request(uri, clazz);
        } catch (PsnUnauthorizedException e) {
            log.warn("PSN returned 401 for {}, retrying with a new access token", uri.getPath());
            psnAuthService.invalidateAccessToken();
            return request(uri, clazz);
        }
    }

    private <T> T request(final URI uri, final Class<T> clazz) {
        final String accessToken = psnAuthService.accessToken();
        rateLimiter.acquire();
        final long startTime = System.currentTimeMillis();
        final T result = webClient.get()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(clazz);
                    }
                    return response.releaseBody().then(Mono.error(error(response.statusCode().value(), uri)));
                })
                .block();
        log.info("PSN request {} took {} ms.", uri.getPath(), System.currentTimeMillis() - startTime);
        return result;
    }

    private PsnApiException error(final int status, final URI uri) {
        final String message = String.format("PSN returned %d for %s", status, uri.getPath());
        if (status == HttpStatus.UNAUTHORIZED.value()) {
            return new PsnUnauthorizedException(message);
        }
        if (status == HttpStatus.FORBIDDEN.value() || status == HttpStatus.NOT_FOUND.value()) {
            return new PsnAccessDeniedException(status, message);
        }
        return new PsnApiException(status, message);
    }

}
