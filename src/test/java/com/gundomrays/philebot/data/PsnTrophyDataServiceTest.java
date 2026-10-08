package com.gundomrays.philebot.data;

import com.gundomrays.philebot.psn.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PsnTrophyDataServiceTest {

    private static final String ACCOUNT_ID = "1234567890";

    private static final String NP_COMMUNICATION_ID = "NPWR20188_00";

    private PsnProfileRepository mockProfileRepository;

    private PsnTitleProgressRepository mockTitleProgressRepository;

    private PsnEarnedTrophyRepository mockEarnedTrophyRepository;

    private PsnTrophyDataService dataService;

    @BeforeEach
    public void setUp() {
        mockProfileRepository = Mockito.mock(PsnProfileRepository.class);
        mockTitleProgressRepository = Mockito.mock(PsnTitleProgressRepository.class);
        mockEarnedTrophyRepository = Mockito.mock(PsnEarnedTrophyRepository.class);
        dataService = new PsnTrophyDataService(mockProfileRepository, mockTitleProgressRepository, mockEarnedTrophyRepository);
    }

    @Test
    public void testBaselineStoresTitlesWithoutTrophies() {
        dataService.saveBaseline(profile(), List.of(title(3)));

        Mockito.verify(mockProfileRepository).save(Mockito.any(PsnProfile.class));
        ArgumentCaptor<PsnTitleProgress> captor = ArgumentCaptor.forClass(PsnTitleProgress.class);
        Mockito.verify(mockTitleProgressRepository).save(captor.capture());
        assertFalse(captor.getValue().isTrophiesTracked());
        assertEquals(3, captor.getValue().getEarnedBronze());
        assertEquals("trophy2", captor.getValue().getNpServiceName());
        Mockito.verifyNoInteractions(mockEarnedTrophyRepository);
    }

    @Test
    public void testTitleProgressStoresOnlyUnknownTrophies() {
        PsnEarnedTrophy known = new PsnEarnedTrophy();
        known.setTrophyId(1);
        Mockito.when(mockEarnedTrophyRepository.findAllByAccountIdAndNpCommunicationId(ACCOUNT_ID, NP_COMMUNICATION_ID))
                .thenReturn(List.of(known));

        dataService.saveTitleProgress(profile(), title(2), null, List.of(earned(1), earned(2)));

        ArgumentCaptor<PsnEarnedTrophy> trophyCaptor = ArgumentCaptor.forClass(PsnEarnedTrophy.class);
        Mockito.verify(mockEarnedTrophyRepository).save(trophyCaptor.capture());
        assertEquals(2, trophyCaptor.getValue().getTrophyId());
        assertEquals("bronze", trophyCaptor.getValue().getTrophyType());

        ArgumentCaptor<PsnTitleProgress> progressCaptor = ArgumentCaptor.forClass(PsnTitleProgress.class);
        Mockito.verify(mockTitleProgressRepository).save(progressCaptor.capture());
        assertTrue(progressCaptor.getValue().isTrophiesTracked());
        assertEquals(2, progressCaptor.getValue().getEarnedBronze());
    }

    @Test
    public void testTitleProgressUpdatesStoredRow() {
        PsnTitleProgress stored = new PsnTitleProgress();
        stored.setEarnedBronze(1);

        dataService.saveTitleProgress(profile(), title(4), stored, List.of());

        Mockito.verify(mockTitleProgressRepository).save(stored);
        assertEquals(4, stored.getEarnedBronze());
        assertEquals(ACCOUNT_ID, stored.getAccountId());
    }

    private PsnProfile profile() {
        PsnProfile profile = new PsnProfile();
        profile.setAccountId(ACCOUNT_ID);
        profile.setOnlineId("player");
        return profile;
    }

    private PsnTrophyTitle title(final int bronze) {
        PsnTrophyCounts counts = new PsnTrophyCounts();
        counts.setBronze(bronze);
        PsnTrophyTitle title = new PsnTrophyTitle();
        title.setNpCommunicationId(NP_COMMUNICATION_ID);
        title.setNpServiceName("trophy2");
        title.setTrophyTitleName("ASTRO's PLAYROOM");
        title.setEarnedTrophies(counts);
        title.setLastUpdatedDateTime(Instant.parse("2026-10-01T10:00:00Z"));
        return title;
    }

    private PsnUserTrophy earned(final int trophyId) {
        PsnUserTrophy trophy = new PsnUserTrophy();
        trophy.setTrophyId(trophyId);
        trophy.setEarned(true);
        trophy.setTrophyType("bronze");
        trophy.setEarnedDateTime(Instant.parse("2026-10-01T10:00:00Z"));
        return trophy;
    }
}
