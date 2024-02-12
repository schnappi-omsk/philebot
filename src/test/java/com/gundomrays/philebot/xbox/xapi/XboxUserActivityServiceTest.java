package com.gundomrays.philebot.xbox.xapi;

import com.gundomrays.philebot.data.XboxProfileRepository;
import com.gundomrays.philebot.data.XboxTitleHistoryDataService;
import com.gundomrays.philebot.xbox.domain.Activity;
import com.gundomrays.philebot.xbox.domain.ActivityItem;
import com.gundomrays.philebot.xbox.domain.Profile;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class XboxUserActivityServiceTest {

    @Mock
    private XApiClient mockXApiClient;

    @Mock
    private XboxProfileRepository mockXboxProfileRepository;

    @Mock
    private XboxTitleHistoryDataService mockXboxTitleHistoryDataService;

    @InjectMocks
    private XboxUserActivityService xboxUserActivityService;

    public XboxUserActivityServiceTest() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void playerActivity_ReturnsActivity_ForValidProfile() {
        // Arrange 
        String id = "TestId";
        Profile testProfile = new Profile();
        testProfile.setId(id);
        
        Activity expectedActivity = new Activity();
        Set<ActivityItem> expectedItems = new TreeSet<>();
        expectedActivity.setActivityItems(expectedItems);
        
        when(mockXApiClient.userActivity(id)).thenReturn(expectedActivity);
        
        // Act
        Activity actualActivity = xboxUserActivityService.playerActivity(testProfile);
        
        // Assert
        assertEquals(expectedActivity, actualActivity);
        verify(mockXApiClient, times(1)).userActivity(id);
        verifyNoMoreInteractions(mockXApiClient, mockXboxProfileRepository, mockXboxTitleHistoryDataService);
    }

    // Additional tests can be added as required.
}