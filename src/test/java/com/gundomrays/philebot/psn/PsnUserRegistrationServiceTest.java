package com.gundomrays.philebot.psn;

import com.gundomrays.philebot.data.PsnTrophyDataService;
import com.gundomrays.philebot.psn.api.PsnApiClient;
import com.gundomrays.philebot.psn.api.exception.PsnAccessDeniedException;
import com.gundomrays.philebot.psn.auth.PsnAuthenticationException;
import com.gundomrays.philebot.psn.domain.PsnProfile;
import com.gundomrays.philebot.psn.domain.PsnTrophyTitle;
import com.gundomrays.philebot.psn.domain.PsnTrophyTitles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PsnUserRegistrationServiceTest {

    private static final String ACCOUNT_ID = "1111111111111111111";

    private PsnApiClient mockApiClient;

    private PsnTrophyDataService mockDataService;

    private PsnUserRegistrationService registrationService;

    @BeforeEach
    public void setUp() {
        mockApiClient = Mockito.mock(PsnApiClient.class);
        mockDataService = Mockito.mock(PsnTrophyDataService.class);
        registrationService = new PsnUserRegistrationService(mockApiClient, mockDataService);
        Mockito.when(mockApiClient.accountId("test_player")).thenReturn(ACCOUNT_ID);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRegistrationStoresBaselineOfAllPages() {
        Mockito.when(mockApiClient.trophyTitles(ACCOUNT_ID, 800, 0)).thenReturn(page(2, 3, "A", "B"));
        Mockito.when(mockApiClient.trophyTitles(ACCOUNT_ID, 800, 2)).thenReturn(page(null, 3, "C"));

        String result = registrationService.registerUser("tg_player", 42L, "test_player");

        assertEquals("tg_player was successfully registered with PSN Online ID test_player", result);
        ArgumentCaptor<PsnProfile> profileCaptor = ArgumentCaptor.forClass(PsnProfile.class);
        ArgumentCaptor<List<PsnTrophyTitle>> titlesCaptor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(mockDataService).saveBaseline(profileCaptor.capture(), titlesCaptor.capture());
        PsnProfile profile = profileCaptor.getValue();
        assertEquals(ACCOUNT_ID, profile.getAccountId());
        assertEquals("test_player", profile.getOnlineId());
        assertEquals("tg_player", profile.getTgUsername());
        assertEquals(42L, profile.getTgId());
        assertTrue(profile.isActive());
        assertFalse(profile.isPing());
        assertEquals(3, titlesCaptor.getValue().size());
    }

    @Test
    public void testAlreadyRegisteredTelegramUser() {
        PsnProfile registered = new PsnProfile();
        registered.setOnlineId("old_player");
        Mockito.when(mockDataService.profileByTgUsername("tg_player")).thenReturn(registered);

        String result = registrationService.registerUser("tg_player", 42L, "test_player");

        assertEquals("User with Telegram username tg_player already has PSN account old_player registered", result);
        Mockito.verifyNoInteractions(mockApiClient);
    }

    @Test
    public void testAlreadyRegisteredAccount() {
        Mockito.when(mockDataService.profileExists(ACCOUNT_ID)).thenReturn(true);

        assertEquals("PSN account test_player is already registered",
                registrationService.registerUser("tg_player", 42L, "test_player"));
        Mockito.verify(mockDataService, Mockito.never()).saveBaseline(Mockito.any(), Mockito.any());
    }

    @Test
    public void testUnknownOnlineId() {
        Mockito.when(mockApiClient.accountId("nobody")).thenThrow(new PsnAccessDeniedException(404, "not found"));

        assertEquals("PSN account nobody was not found", registrationService.registerUser("tg_player", 42L, "nobody"));
    }

    @Test
    public void testHiddenTrophies() {
        Mockito.when(mockApiClient.trophyTitles(ACCOUNT_ID, 800, 0)).thenThrow(new PsnAccessDeniedException(403, "denied"));

        String result = registrationService.registerUser("tg_player", 42L, "test_player");

        assertTrue(result.startsWith("Trophies of test_player are not visible to the bot account"));
        Mockito.verify(mockDataService, Mockito.never()).saveBaseline(Mockito.any(), Mockito.any());
    }

    @Test
    public void testAuthenticationFailure() {
        Mockito.when(mockApiClient.accountId("test_player")).thenThrow(new PsnAuthenticationException("expired"));

        assertEquals("PSN is not available now, try again later.",
                registrationService.registerUser("tg_player", 42L, "test_player"));
    }

    @Test
    public void testMissingOnlineId() {
        assertEquals("Format: /psnreg [PSN Online ID]", registrationService.registerUser("tg_player", 42L, " "));
        Mockito.verifyNoInteractions(mockApiClient);
    }

    private PsnTrophyTitles page(final Integer nextOffset, final int total, final String... ids) {
        PsnTrophyTitles page = new PsnTrophyTitles();
        for (String id : ids) {
            PsnTrophyTitle title = new PsnTrophyTitle();
            title.setNpCommunicationId(id);
            page.getTrophyTitles().add(title);
        }
        page.setNextOffset(nextOffset);
        page.setTotalItemCount(total);
        return page;
    }
}
