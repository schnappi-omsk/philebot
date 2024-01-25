package com.gundomrays.philebot.xbox.xapi;

import com.gundomrays.philebot.data.XboxProfileRepository;
import com.gundomrays.philebot.data.XboxTitleHistoryRepository;
import com.gundomrays.philebot.xbox.domain.Profile;
import com.gundomrays.philebot.xbox.domain.TitleHistory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * XBoxUserRegistrationServiceTest
 * This class tests the registerUser method of the XBoxUserRegistrationService class
 */
class XBoxUserRegistrationServiceTest {

    private XBoxUserRegistrationService xBoxUserRegistrationService;
    private XApiClient xApiClientMock = Mockito.mock(XApiClient.class);
    private XboxProfileRepository xboxProfileRepositoryMock = Mockito.mock(XboxProfileRepository.class);
    private XboxTitleHistoryRepository xboxTitleHistoryRepositoryMock = Mockito.mock(XboxTitleHistoryRepository.class);

    @BeforeEach
    void setUp() {
        xBoxUserRegistrationService = new XBoxUserRegistrationService(xApiClientMock, xboxProfileRepositoryMock, xboxTitleHistoryRepositoryMock);
    }

    @AfterEach
    void tearDown() {
        xBoxUserRegistrationService = null;
    }

    /**
     * This test verifies that registration is successful when the user does not already exist in the repository.
     */
    @Test
    void registerUser_NotExist_Success() {
        String tgUserName = "user1";
        Long tgId = 1L;
        String gamerTag = "gamer1";
        Profile xboxProfile = new Profile();

        when(xboxProfileRepositoryMock.findByTgUsername(tgUserName)).thenReturn(Optional.empty());
        when(xApiClientMock.userByGamertag(gamerTag)).thenReturn(xboxProfile);
        when(xApiClientMock.titleHistory(any())).thenReturn(new TitleHistory());

        XboxServiceResponse actualResponse = xBoxUserRegistrationService.registerUser(tgUserName, tgId, gamerTag);

        assertNotNull(actualResponse);
        assertEquals(String.format("%s was successfully registered with gamertag %s", tgUserName, gamerTag),
                actualResponse.getText());
    }

    /**
     * This test verifies that registration is not successful when the user already exist in the repository.
     */
    @Test
    void registerUser_AlreadyExist_Failure() {
        String tgUserName = "user1";
        Long tgId = 1L;
        String gamerTag = "gamer1";
        Profile xboxProfile = new Profile();

        when(xboxProfileRepositoryMock.findByTgUsername(tgUserName)).thenReturn(Optional.of(xboxProfile));

        XboxServiceResponse actualResponse = xBoxUserRegistrationService.registerUser(tgUserName, tgId, gamerTag);

        assertNotNull(actualResponse);
        assertEquals(String.format("User with Telegram username %s is already registered", tgUserName),
                actualResponse.getText());
    }
}