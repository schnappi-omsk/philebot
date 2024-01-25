package com.gundomrays.philebot.worker;

import com.gundomrays.philebot.xbox.domain.ActivityItem;
import com.gundomrays.philebot.xbox.xapi.XBoxUserRegistrationService;
import com.gundomrays.philebot.xbox.domain.Profile;
import com.gundomrays.philebot.xbox.xapi.XboxAchievementRetrieveService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PhilAchievementRetrieverTest {

    private ActivityItem createMockActivityItem() {
        ActivityItem item = new ActivityItem();
        item.setUserXuid("xuid");
        item.setAchievementName("achievementName");
        item.setAchievementDescription("achievementDescription");
        item.setContentTitle("contentTitle");
        item.setGamerscore(100);
        item.setRarityPercentage(25);
        item.setAchievementIcon("icon");
        return item;
    }

    private Profile createUserProfile(String username, Long id) {
        Profile userProfile = new Profile();
        userProfile.setTgUsername(username);
        userProfile.setTgId(id);
        return userProfile;
    }

    @Test
    public void retrieveTest() throws Exception {
        // Given
        XboxAchievementRetrieveService xboxAchievementRetrieveService = mock(XboxAchievementRetrieveService.class);
        XBoxUserRegistrationService xBoxUserRegistrationService = mock(XBoxUserRegistrationService.class);
        PhilAchievementRetriever philAchievementRetriever = new PhilAchievementRetriever(xboxAchievementRetrieveService, xBoxUserRegistrationService);

        String value = "localhost";
        Field field = PhilAchievementRetriever.class.getDeclaredField("serviceHost");
        field.setAccessible(true);
        field.set(philAchievementRetriever, value);

        List<ActivityItem> items = new ArrayList<>();
        items.add(createMockActivityItem());

        Profile userProfile = createUserProfile("tgUsername", 1L);

        when(xboxAchievementRetrieveService.newAchievements()).thenReturn(items);
        when(xBoxUserRegistrationService.retrieveUserProfile(anyString())).thenReturn(userProfile);

        // When
        Collection<String> result = philAchievementRetriever.retrieve();
        String item = result.iterator().next();

        // Then
        assertEquals(1, result.size());
        String expectedLinkStart = "<a href='tg://user?id=1'>@tgUsername</a> — <a href='localhost/xbox/achievementName/achievementDescription/100/25?imgUrl=icon&seed=";
        String expectedLinkEnd = "'>contentTitle</a>";
        assertTrue(item.startsWith(expectedLinkStart));
        assertTrue(item.endsWith(expectedLinkEnd));

        String seed = item.substring(expectedLinkStart.length(), item.indexOf(expectedLinkEnd ) - 1);
        assertDoesNotThrow(() -> UUID.fromString(seed));
    }


}