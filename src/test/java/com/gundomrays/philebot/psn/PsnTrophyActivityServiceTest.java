package com.gundomrays.philebot.psn;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gundomrays.philebot.data.PsnTrophyDataService;
import com.gundomrays.philebot.messaging.MessageQueue;
import com.gundomrays.philebot.psn.api.PsnApiClient;
import com.gundomrays.philebot.psn.api.exception.PsnAccessDeniedException;
import com.gundomrays.philebot.psn.auth.PsnAuthService;
import com.gundomrays.philebot.psn.auth.PsnAuthenticationException;
import com.gundomrays.philebot.psn.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class PsnTrophyActivityServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-08T12:00:00Z");

    private static final String ACCOUNT_ID = "1111111111111111111";

    private static final String ASTRO = "NPWR20188_00";

    private static final String RATCHET = "NPWR10600_00";

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private PsnApiClient mockApiClient;

    private PsnAuthService mockAuthService;

    private PsnTrophyDataService mockDataService;

    private MessageQueue mockMessageQueue;

    private PsnTrophyQueue trophyQueue;

    private PsnTrophyActivityService activityService;

    private PsnProfile player;

    @BeforeEach
    public void setUp() throws Exception {
        mockApiClient = Mockito.mock(PsnApiClient.class);
        mockAuthService = Mockito.mock(PsnAuthService.class);
        mockDataService = Mockito.mock(PsnTrophyDataService.class);
        mockMessageQueue = Mockito.mock(MessageQueue.class);
        trophyQueue = new PsnTrophyQueue();
        activityService = activityService(5, "bronze");

        player = new PsnProfile();
        player.setAccountId(ACCOUNT_ID);
        player.setOnlineId("test_player");
        player.setTgUsername("tg_player");
        player.setRegisteredAt(Instant.parse("2026-09-01T00:00:00Z"));

        Mockito.when(mockDataService.profiles()).thenReturn(List.of(player));
        Mockito.when(mockApiClient.trophyTitles(ACCOUNT_ID, 50, 0)).thenReturn(fixture("trophy-titles.json", PsnTrophyTitles.class));
        Mockito.when(mockApiClient.earnedTrophies(ACCOUNT_ID, ASTRO, "trophy2"))
                .thenReturn(fixture("earned-trophies.json", PsnUserTrophies.class));
        Mockito.when(mockApiClient.titleTrophies(ASTRO, "trophy2"))
                .thenReturn(fixture("title-trophies.json", PsnTrophyDefinitions.class));
    }

    @Test
    public void testUnchangedTitlesAreNotFetched() {
        storeProgress(progress(ASTRO, 5, 1, 0, 0, false, null), progress(RATCHET, 40, 5, 2, 1, true, null));

        activityService.allPlayersLatestTrophies();

        Mockito.verify(mockApiClient, Mockito.never()).earnedTrophies(Mockito.any(), Mockito.any(), Mockito.any());
        assertNull(trophyQueue.takeTrophy());
    }

    @Test
    public void testBaselineTitleAnnouncesTrophiesEarnedAfterLastUpdate() {
        PsnTitleProgress astro = progress(ASTRO, 4, 1, 0, 0, false, Instant.parse("2026-10-01T00:00:00Z"));
        storeProgress(astro, progress(RATCHET, 40, 5, 2, 1, true, null));

        activityService.allPlayersLatestTrophies();

        PsnTrophy trophy = trophyQueue.takeTrophy();
        assertNotNull(trophy);
        assertEquals("Hidden <Hero>", trophy.getTrophyName());
        assertEquals("Find the secret & win.", trophy.getTrophyDetail());
        assertEquals(PsnTrophyType.GOLD, trophy.getType());
        assertEquals("3.7", trophy.getEarnedRate());
        assertEquals("ASTRO's PLAYROOM", trophy.getTitleName());
        assertNull(trophyQueue.takeTrophy());

        Mockito.verify(mockDataService).saveTitleProgress(Mockito.eq(player), Mockito.argThat(title -> ASTRO.equals(title.getNpCommunicationId())),
                Mockito.eq(astro), Mockito.argThat(earned -> earned.size() == 2));
    }

    @Test
    public void testTrackedTitleAnnouncesUnknownTrophies() {
        storeProgress(progress(ASTRO, 4, 0, 0, 0, true, null), progress(RATCHET, 40, 5, 2, 1, true, null));
        Mockito.when(mockDataService.earnedTrophyIds(ACCOUNT_ID, ASTRO)).thenReturn(Set.of(2));

        activityService.allPlayersLatestTrophies();

        PsnTrophy trophy = trophyQueue.takeTrophy();
        assertEquals("First Steps", trophy.getTrophyName());
        assertEquals(PsnTrophyType.BRONZE, trophy.getType());
        assertNull(trophyQueue.takeTrophy());
    }

    @Test
    public void testNewTitleAnnouncesTrophiesEarnedAfterRegistration() {
        player.setRegisteredAt(Instant.parse("2026-10-01T00:00:00Z"));
        storeProgress(progress(RATCHET, 40, 5, 2, 1, true, null));

        activityService.allPlayersLatestTrophies();

        assertEquals("Hidden <Hero>", trophyQueue.takeTrophy().getTrophyName());
        assertNull(trophyQueue.takeTrophy());
    }

    @Test
    public void testNewTitleWithoutTrophiesIsStoredWithoutRequests() throws Exception {
        PsnTrophyTitles titles = fixture("trophy-titles.json", PsnTrophyTitles.class);
        titles.getTrophyTitles().getFirst().setEarnedTrophies(new PsnTrophyCounts());
        Mockito.when(mockApiClient.trophyTitles(ACCOUNT_ID, 50, 0)).thenReturn(titles);
        storeProgress(progress(RATCHET, 40, 5, 2, 1, true, null));

        activityService.allPlayersLatestTrophies();

        Mockito.verify(mockApiClient, Mockito.never()).earnedTrophies(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(mockDataService).saveTitleProgress(Mockito.eq(player), Mockito.any(), Mockito.isNull(), Mockito.eq(List.of()));
    }

    @Test
    public void testDefinitionsAreCached() {
        storeProgress(progress(ASTRO, 4, 1, 0, 0, false, Instant.parse("2026-10-01T00:00:00Z")),
                progress(RATCHET, 40, 5, 2, 1, true, null));

        activityService.allPlayersLatestTrophies();
        activityService.allPlayersLatestTrophies();

        Mockito.verify(mockApiClient, Mockito.times(2)).earnedTrophies(ACCOUNT_ID, ASTRO, "trophy2");
        Mockito.verify(mockApiClient, Mockito.times(1)).titleTrophies(ASTRO, "trophy2");
    }

    @Test
    public void testInactivePlayerIsSkipped() {
        player.setActive(false);

        activityService.allPlayersLatestTrophies();

        Mockito.verifyNoInteractions(mockApiClient);
    }

    @Test
    public void testHiddenProfileDoesNotStopOtherPlayers() {
        PsnProfile hidden = new PsnProfile();
        hidden.setAccountId("2222222222222222222");
        hidden.setOnlineId("hidden_player");
        Mockito.when(mockDataService.profiles()).thenReturn(List.of(hidden, player));
        Mockito.when(mockApiClient.trophyTitles(hidden.getAccountId(), 50, 0))
                .thenThrow(new PsnAccessDeniedException(403, "denied"));
        storeProgress(progress(ASTRO, 4, 1, 0, 0, false, Instant.parse("2026-10-01T00:00:00Z")),
                progress(RATCHET, 40, 5, 2, 1, true, null));

        activityService.allPlayersLatestTrophies();

        assertNotNull(trophyQueue.takeTrophy());
    }

    @Test
    public void testAuthenticationFailurePausesPolling() {
        Mockito.when(mockApiClient.trophyTitles(ACCOUNT_ID, 50, 0)).thenThrow(new PsnAuthenticationException("expired"));

        activityService.allPlayersLatestTrophies();
        activityService.allPlayersLatestTrophies();

        Mockito.verify(mockApiClient, Mockito.times(1)).trophyTitles(ACCOUNT_ID, 50, 0);
        Mockito.verify(mockMessageQueue, Mockito.times(1)).messageToSend("PSN auth failed");
    }

    @Test
    public void testRefreshTokenExpiryIsAnnouncedOnce() {
        storeProgress(progress(ASTRO, 5, 1, 0, 0, false, null), progress(RATCHET, 40, 5, 2, 1, true, null));
        Mockito.when(mockAuthService.refreshTokenExpiresAt()).thenReturn(Instant.parse("2026-10-10T08:00:00Z"));

        activityService.allPlayersLatestTrophies();
        activityService.allPlayersLatestTrophies();

        Mockito.verify(mockMessageQueue, Mockito.times(1)).messageToSend("PSN expires 10.10.2026");
    }

    @Test
    public void testDistantRefreshTokenExpiryIsNotAnnounced() {
        storeProgress(progress(ASTRO, 5, 1, 0, 0, false, null), progress(RATCHET, 40, 5, 2, 1, true, null));
        Mockito.when(mockAuthService.refreshTokenExpiresAt()).thenReturn(Instant.parse("2026-12-01T00:00:00Z"));

        activityService.allPlayersLatestTrophies();

        Mockito.verifyNoInteractions(mockMessageQueue);
    }

    @Test
    public void testNoPlayersMeansNoRequests() {
        Mockito.when(mockDataService.profiles()).thenReturn(List.of());

        activityService.allPlayersLatestTrophies();

        Mockito.verifyNoInteractions(mockApiClient, mockAuthService);
    }

    @Test
    public void testAnnouncedKeepsNewestUpToLimitAndPlatinum() {
        PsnTrophyActivityService limited = activityService(2, "bronze");
        List<PsnUserTrophy> trophies = List.of(
                trophy(1, "platinum", "2026-10-01T10:00:00Z"),
                trophy(2, "bronze", "2026-10-01T11:00:00Z"),
                trophy(3, "bronze", "2026-10-01T12:00:00Z"),
                trophy(4, "silver", "2026-10-01T13:00:00Z"));

        List<Integer> announced = limited.announced(trophies).stream().map(PsnUserTrophy::getTrophyId).toList();

        assertEquals(List.of(1, 4), announced);
    }

    @Test
    public void testAnnouncedSkipsTypesBelowMinimum() {
        PsnTrophyActivityService goldOnly = activityService(5, "gold");
        List<PsnUserTrophy> trophies = List.of(
                trophy(1, "bronze", "2026-10-01T10:00:00Z"),
                trophy(2, "gold", "2026-10-01T11:00:00Z"),
                trophy(3, "platinum", "2026-10-01T12:00:00Z"),
                trophy(4, "silver", "2026-10-01T13:00:00Z"));

        List<Integer> announced = goldOnly.announced(trophies).stream().map(PsnUserTrophy::getTrophyId).toList();

        assertEquals(List.of(2, 3), announced);
    }

    private PsnTrophyActivityService activityService(final int limitPerUser, final String minTrophyType) {
        return new PsnTrophyActivityService(mockApiClient, mockAuthService, mockDataService, trophyQueue, mockMessageQueue,
                Clock.fixed(NOW, ZoneOffset.UTC), 50, limitPerUser, minTrophyType, 7, "PSN expires %s", "PSN auth failed");
    }

    private void storeProgress(final PsnTitleProgress... progress) {
        Map<String, PsnTitleProgress> stored = new HashMap<>();
        Arrays.stream(progress).forEach(p -> stored.put(p.getNpCommunicationId(), p));
        Mockito.when(mockDataService.titleProgress(ACCOUNT_ID)).thenReturn(stored);
    }

    private PsnTitleProgress progress(final String npCommunicationId, final int bronze, final int silver, final int gold,
                                      final int platinum, final boolean tracked, final Instant lastUpdated) {
        PsnTitleProgress progress = new PsnTitleProgress();
        progress.setAccountId(ACCOUNT_ID);
        progress.setNpCommunicationId(npCommunicationId);
        progress.setEarnedBronze(bronze);
        progress.setEarnedSilver(silver);
        progress.setEarnedGold(gold);
        progress.setEarnedPlatinum(platinum);
        progress.setTrophiesTracked(tracked);
        progress.setLastUpdated(lastUpdated);
        return progress;
    }

    private PsnUserTrophy trophy(final int id, final String type, final String earnedAt) {
        PsnUserTrophy trophy = new PsnUserTrophy();
        trophy.setTrophyId(id);
        trophy.setTrophyType(type);
        trophy.setEarned(true);
        trophy.setEarnedDateTime(Instant.parse(earnedAt));
        return trophy;
    }

    private <T> T fixture(final String name, final Class<T> clazz) throws Exception {
        return objectMapper.readValue(StubExchange.resource(name), clazz);
    }
}
