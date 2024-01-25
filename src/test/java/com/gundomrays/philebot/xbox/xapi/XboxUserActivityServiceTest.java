package com.gundomrays.philebot.xbox.xapi;

import com.gundomrays.philebot.data.XboxProfileRepository;
import com.gundomrays.philebot.xbox.domain.Activity;
import com.gundomrays.philebot.xbox.domain.Profile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class XboxUserActivityServiceTest {

    @Mock
    private XApiClient xApiClient;

    @Mock
    private XboxProfileRepository xboxProfileRepository;

    private XboxUserActivityService xboxUserActivityService;

    @BeforeEach
    public void setUp() {
        xboxUserActivityService = new XboxUserActivityService(xApiClient, xboxProfileRepository);
    }

    @Test
    public void testPlayerActivityWithProfile() {
        Profile testProfile = createProfile("testGamertag");

        Activity expectedActivity = new Activity();
        when(xApiClient.userActivity(testProfile.getId())).thenReturn(expectedActivity);
        when(xboxProfileRepository.findByGamertag(anyString())).thenReturn(java.util.Optional.of(testProfile));

        Activity result = xboxUserActivityService.playerActivity(testProfile);

        assertEquals(expectedActivity, result);
        verify(xApiClient).userActivity(testProfile.getId());
        verify(xboxProfileRepository).findByGamertag(testProfile.getGamertag());
    }

    @Test
    public void testPlayerActivityWithGamertag() {
        String gamertag = "testGamertag";
        Profile xboxProfile = createProfile(gamertag);

        Activity expectedActivity = new Activity();
        when(xApiClient.userActivity(xboxProfile.getId())).thenReturn(expectedActivity);
        when(xboxProfileRepository.findByGamertag(anyString())).thenReturn(java.util.Optional.of(xboxProfile));

        Activity result = xboxUserActivityService.playerActivity(gamertag);

        assertEquals(expectedActivity, result);
        verify(xApiClient).userActivity(xboxProfile.getId());
        verify(xboxProfileRepository).findByGamertag(gamertag);
    }

    @Test
    public void testPlayerActivityWithNotRegisteredGamertag() {
        String gamertag = "unregisteredGamertag";

        when(xboxProfileRepository.findByGamertag(anyString())).thenReturn(java.util.Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            xboxUserActivityService.playerActivity(gamertag);
        });

        String expectedMessage = "User is not registered in the app: " + gamertag;
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }
    
    private Profile createProfile(String gamertag) {
        Profile profile = new Profile();
        profile.setGamertag(gamertag);
        return profile;
    }
}