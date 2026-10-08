package com.gundomrays.philebot.psn.api;

import com.gundomrays.philebot.psn.StubExchange;
import com.gundomrays.philebot.psn.api.exception.PsnAccessDeniedException;
import com.gundomrays.philebot.psn.api.exception.PsnApiException;
import com.gundomrays.philebot.psn.api.exception.PsnUnauthorizedException;
import com.gundomrays.philebot.psn.auth.PsnAuthService;
import com.gundomrays.philebot.psn.domain.PsnTrophyDefinitions;
import com.gundomrays.philebot.psn.domain.PsnTrophyTitle;
import com.gundomrays.philebot.psn.domain.PsnTrophyTitles;
import com.gundomrays.philebot.psn.domain.PsnUserTrophies;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class PsnApiClientTest {

    private static final String ACCOUNT_ID = "1111111111111111111";

    private StubExchange stub;

    private PsnAuthService mockAuthService;

    private PsnApiClient apiClient;

    @BeforeEach
    public void setUp() {
        stub = new StubExchange();
        mockAuthService = Mockito.mock(PsnAuthService.class);
        Mockito.when(mockAuthService.accessToken()).thenReturn("test-access-token");
        apiClient = new PsnApiClient(stub.builder(), mockAuthService, "https://localhost/api",
                "https://localhost/profile", "test-agent", "ru-RU", 6000.0);
    }

    @Test
    public void testTrophyTitles() {
        stub.fixture("trophy-titles.json");

        PsnTrophyTitles titles = apiClient.trophyTitles(ACCOUNT_ID, 50, 0);

        ClientRequest request = stub.requests().getFirst();
        assertEquals("/api/trophy/v1/users/" + ACCOUNT_ID + "/trophyTitles", request.url().getPath());
        assertEquals("limit=50&offset=0", request.url().getQuery());
        assertEquals("Bearer test-access-token", request.headers().getFirst(HttpHeaders.AUTHORIZATION));
        assertEquals("ru-RU", request.headers().getFirst(HttpHeaders.ACCEPT_LANGUAGE));
        assertEquals("test-agent", request.headers().getFirst(HttpHeaders.USER_AGENT));

        assertEquals(2, titles.getTrophyTitles().size());
        assertEquals(2, titles.getNextOffset());
        PsnTrophyTitle astro = titles.getTrophyTitles().getFirst();
        assertEquals("trophy2", astro.getNpServiceName());
        assertEquals("NPWR20188_00", astro.getNpCommunicationId());
        assertEquals(5, astro.getEarnedTrophies().getBronze());
        assertEquals(1, astro.getDefinedTrophies().getPlatinum());
        assertEquals(Instant.parse("2026-10-01T21:58:18Z"), astro.getLastUpdatedDateTime());
    }

    @Test
    public void testEarnedTrophies() {
        stub.fixture("earned-trophies.json");

        PsnUserTrophies trophies = apiClient.earnedTrophies(ACCOUNT_ID, "NPWR10600_00", "trophy");

        ClientRequest request = stub.requests().getFirst();
        assertEquals("/api/trophy/v1/users/" + ACCOUNT_ID + "/npCommunicationIds/NPWR10600_00/trophyGroups/all/trophies",
                request.url().getPath());
        assertEquals("npServiceName=trophy", request.url().getQuery());

        assertEquals(4, trophies.getTrophies().size());
        assertTrue(trophies.getTrophies().get(2).isEarned());
        assertEquals(Instant.parse("2026-10-01T21:58:18Z"), trophies.getTrophies().get(2).getEarnedDateTime());
        assertEquals("3.7", trophies.getTrophies().get(2).getTrophyEarnedRate());
        assertNull(trophies.getTrophies().get(0).getEarnedDateTime());
    }

    @Test
    public void testTitleTrophies() {
        stub.fixture("title-trophies.json");

        PsnTrophyDefinitions definitions = apiClient.titleTrophies("NPWR20188_00", "trophy2");

        assertEquals("/api/trophy/v1/npCommunicationIds/NPWR20188_00/trophyGroups/all/trophies",
                stub.requests().getFirst().url().getPath());
        assertEquals("01.00", definitions.getTrophySetVersion());
        assertEquals("Hidden <Hero>", definitions.getTrophies().get(2).getTrophyName());
    }

    @Test
    public void testAccountId() {
        stub.fixture("profile-lookup.json");

        assertEquals(ACCOUNT_ID, apiClient.accountId("test_player"));

        ClientRequest request = stub.requests().getFirst();
        assertEquals("/profile/test_player/profile2", request.url().getPath());
        assertEquals("fields=accountId,onlineId,currentOnlineId", request.url().getQuery());
    }

    @Test
    public void testUnknownOnlineId() {
        stub.status(HttpStatus.NOT_FOUND, "{\"error\":{\"code\":2105356}}");

        assertThrows(PsnAccessDeniedException.class, () -> apiClient.accountId("nobody"));
    }

    @Test
    public void testUnauthorizedRequestIsRetriedOnce() {
        stub.status(HttpStatus.UNAUTHORIZED, "{}").fixture("trophy-titles.json");

        assertEquals(2, apiClient.trophyTitles(ACCOUNT_ID, 50, 0).getTrophyTitles().size());

        Mockito.verify(mockAuthService).invalidateAccessToken();
        assertEquals(2, stub.requests().size());
    }

    @Test
    public void testSecondUnauthorizedIsThrown() {
        stub.status(HttpStatus.UNAUTHORIZED, "{}").status(HttpStatus.UNAUTHORIZED, "{}");

        assertThrows(PsnUnauthorizedException.class, () -> apiClient.trophyTitles(ACCOUNT_ID, 50, 0));
    }

    @Test
    public void testHiddenTrophiesAreAccessDenied() {
        stub.status(HttpStatus.FORBIDDEN, "{\"error\":{\"code\":2240526,\"message\":\"Not permitted by access control\"}}");

        PsnAccessDeniedException e = assertThrows(PsnAccessDeniedException.class,
                () -> apiClient.trophyTitles(ACCOUNT_ID, 50, 0));
        assertEquals(403, e.getStatus());
    }

    @Test
    public void testTooManyRequests() {
        stub.status(HttpStatus.TOO_MANY_REQUESTS, "{}");

        PsnApiException e = assertThrows(PsnApiException.class, () -> apiClient.trophyTitles(ACCOUNT_ID, 50, 0));
        assertEquals(429, e.getStatus());
    }
}
