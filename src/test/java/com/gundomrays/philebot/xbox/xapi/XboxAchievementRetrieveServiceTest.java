package com.gundomrays.philebot.xbox.xapi;

import com.gundomrays.philebot.xbox.domain.ActivityItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class XboxAchievementRetrieveServiceTest {

    private final XboxUserActivityService activityService = mock(XboxUserActivityService.class);

    private final ActivityItem mockActivityItem = mock(ActivityItem.class);

    @Test
    @DisplayName("Test newAchievements method")
    void newAchievementsTest() {
        // Setup
        XboxAchievementRetrieveService xboxAchievementRetrieveService = new XboxAchievementRetrieveService(activityService);
        LocalDateTime testDateTime = LocalDateTime.now();

        when(mockActivityItem.getUserXuid()).thenReturn("123");
        when(mockActivityItem.getContentTitle()).thenReturn("Test Title");
        when(mockActivityItem.getTitleId()).thenReturn("Test Id");
        when(mockActivityItem.getDate()).thenReturn(testDateTime);

        when(activityService.allPlayersLatestAchievements()).thenReturn(List.of(mockActivityItem));

        // Call method and assert expected results
        Collection<ActivityItem> result = xboxAchievementRetrieveService.newAchievements();
        assertThat(result).hasSize(1);
        
        // Extracting result to a variable
        ActivityItem retrievedActivityItem = result.iterator().next();
        assertThat(retrievedActivityItem.getUserXuid()).isEqualTo("123");
        assertThat(retrievedActivityItem.getContentTitle()).isEqualTo("Test Title");
        assertThat(retrievedActivityItem.getTitleId()).isEqualTo("Test Id");
        assertThat(retrievedActivityItem.getDate()).isEqualTo(testDateTime);

        //Verify if method of activityService is called
        verify(activityService, Mockito.times(1)).allPlayersLatestAchievements();

        reset(mockActivityItem, activityService);
    }
}