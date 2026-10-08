package com.gundomrays.philebot.psn.auth;

import com.gundomrays.philebot.psn.StubExchange;
import com.gundomrays.philebot.telegram.data.SettingsRepository;
import com.gundomrays.philebot.telegram.domain.Settings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class PsnAuthServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-08T12:00:00Z");

    private static final String REDIRECT = "com.example.test://redirect";

    private final Map<String, Settings> settings = new HashMap<>();

    private StubExchange stub;

    private MutableClock clock;

    @BeforeEach
    public void setUp() {
        stub = new StubExchange();
        clock = new MutableClock(NOW);
    }

    @Test
    public void testNpssoIsExchangedForTokens() {
        stub.redirect(REDIRECT + "/?code=v3.TESTCODE&cid=abc").fixture("token.json");

        assertEquals("test-access-token", authService("test-npsso").accessToken());

        ClientRequest authorize = stub.requests().get(0);
        assertEquals(HttpMethod.GET, authorize.method());
        assertEquals("/auth/authorize", authorize.url().getPath());
        assertEquals("npsso=test-npsso", authorize.headers().getFirst(HttpHeaders.COOKIE));
        assertTrue(authorize.url().getRawQuery().contains("client_id=test-client"));
        assertTrue(authorize.url().getRawQuery().contains("access_type=offline"));

        ClientRequest token = stub.requests().get(1);
        assertEquals(HttpMethod.POST, token.method());
        assertEquals("/auth/token", token.url().getPath());
        assertEquals("Basic dGVzdA==", token.headers().getFirst(HttpHeaders.AUTHORIZATION));
        String body = StubExchange.body(token);
        assertTrue(body.contains("code=v3.TESTCODE"));
        assertTrue(body.contains("grant_type=authorization_code"));

        assertEquals("test-refresh-token", settings.get(PsnAuthService.REFRESH_TOKEN_KEY).getValue());
        assertEquals(String.valueOf(NOW.plusSeconds(5183999).getEpochSecond()),
                settings.get(PsnAuthService.REFRESH_EXPIRES_AT_KEY).getValue());
    }

    @Test
    public void testAccessTokenIsReusedUntilExpiry() {
        stub.redirect(REDIRECT + "/?code=v3.TESTCODE").fixture("token.json").fixture("token.json");
        PsnAuthService authService = authService("test-npsso");

        authService.accessToken();
        authService.accessToken();
        assertEquals(2, stub.requests().size());

        clock.set(NOW.plusSeconds(3599));
        authService.accessToken();
        assertEquals(3, stub.requests().size());
        assertTrue(StubExchange.body(stub.requests().get(2)).contains("grant_type=refresh_token"));
    }

    @Test
    public void testStoredRefreshTokenIsUsedWithoutNpsso() {
        storeRefreshToken("stored-refresh-token", NOW.plusSeconds(3600));
        stub.fixture("token.json");

        assertEquals("test-access-token", authService("").accessToken());

        assertEquals(1, stub.requests().size());
        String body = StubExchange.body(stub.requests().getFirst());
        assertTrue(body.contains("refresh_token=stored-refresh-token"));
        assertTrue(body.contains("scope=test-scope"));
    }

    @Test
    public void testRejectedRefreshTokenFallsBackToNpsso() {
        storeRefreshToken("stored-refresh-token", NOW.plusSeconds(3600));
        stub.status(HttpStatus.BAD_REQUEST, "{\"error\":\"invalid_grant\",\"error_code\":4150}")
                .redirect(REDIRECT + "/?code=v3.TESTCODE")
                .fixture("token.json");

        assertEquals("test-access-token", authService("test-npsso").accessToken());
        assertEquals(3, stub.requests().size());
    }

    @Test
    public void testExpiredRefreshTokenIsNotUsed() {
        storeRefreshToken("stored-refresh-token", NOW.minusSeconds(1));
        stub.redirect(REDIRECT + "/?code=v3.TESTCODE").fixture("token.json");

        authService("test-npsso").accessToken();

        assertEquals(HttpMethod.GET, stub.requests().getFirst().method());
    }

    @Test
    public void testExpiredNpssoIsReported() {
        stub.redirect(REDIRECT + "/?error=login_required&error_code=4165&error_description=Login+required");

        PsnAuthenticationException e = assertThrows(PsnAuthenticationException.class,
                () -> authService("test-npsso").accessToken());
        assertTrue(e.getMessage().contains("expired"));
    }

    @Test
    public void testRejectedClientIsReportedWithoutBody() {
        stub.redirect(REDIRECT + "/?code=v3.TESTCODE")
                .status(HttpStatus.UNAUTHORIZED, "{\"error\":\"invalid_client\",\"error_code\":4102}");

        PsnAuthenticationException e = assertThrows(PsnAuthenticationException.class,
                () -> authService("test-npsso").accessToken());
        assertEquals("PSN token request failed with status 401, error code 4102", e.getMessage());
    }

    @Test
    public void testMissingCredentialsFailWithoutRequests() {
        assertThrows(PsnAuthenticationException.class, () -> authService("").accessToken());
        assertTrue(stub.requests().isEmpty());
    }

    @Test
    public void testRefreshTokenExpiry() {
        assertNull(authService("").refreshTokenExpiresAt());

        storeRefreshToken("stored-refresh-token", NOW.plusSeconds(60));
        assertEquals(NOW.plusSeconds(60), authService("").refreshTokenExpiresAt());
    }

    private PsnAuthService authService(final String npsso) {
        SettingsRepository settingsRepository = Mockito.mock(SettingsRepository.class);
        Mockito.when(settingsRepository.findById(Mockito.anyString()))
                .thenAnswer(invocation -> Optional.ofNullable(settings.get(invocation.<String>getArgument(0))));
        Mockito.when(settingsRepository.save(Mockito.any(Settings.class))).thenAnswer(invocation -> {
            Settings setting = invocation.getArgument(0);
            settings.put(setting.getId(), setting);
            return setting;
        });
        return new PsnAuthService(stub.builder(), settingsRepository, clock, "https://localhost/auth", npsso,
                "test-client", "dGVzdA==", REDIRECT, "test-scope", "test-agent");
    }

    private void storeRefreshToken(final String token, final Instant expiresAt) {
        Settings tokenSetting = new Settings();
        tokenSetting.setId(PsnAuthService.REFRESH_TOKEN_KEY);
        tokenSetting.setValue(token);
        settings.put(tokenSetting.getId(), tokenSetting);
        Settings expirySetting = new Settings();
        expirySetting.setId(PsnAuthService.REFRESH_EXPIRES_AT_KEY);
        expirySetting.setValue(String.valueOf(expiresAt.getEpochSecond()));
        settings.put(expirySetting.getId(), expirySetting);
    }

    private static class MutableClock extends Clock {

        private Instant instant;

        MutableClock(Instant instant) {
            this.instant = instant;
        }

        void set(Instant instant) {
            this.instant = instant;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
