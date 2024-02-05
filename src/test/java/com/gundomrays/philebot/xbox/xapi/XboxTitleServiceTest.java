package com.gundomrays.philebot.xbox.xapi;

import com.gundomrays.philebot.data.XboxTitleDataService;
import com.gundomrays.philebot.xbox.domain.Title;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * XboxTitleServiceTest.java
 * This class tests the searchGames() method in the XboxTitleService class to
 * ensure it functions as expected based on a set of known inputs and expected outputs.
 */
public class XboxTitleServiceTest {

    @Test
    void testSearchGames() {
        // Instantiate dependencies
        XboxTitleDataService xboxTitleDataService = Mockito.mock(XboxTitleDataService.class);

        // Given
        String userInput = "userInput";
        String titleId1 = "1";
        String titleId2 = "2";
        String titleName1 = "Game1";
        String titleName2 = "Game2";
        String titlePlatform1 = "PC";
        String titlePlatform2 = "XBox";
        Title title1 = new Title();
        title1.setTitleId(titleId1);
        title1.setName(titleName1);
        title1.setPlatform(titlePlatform1);
        Title title2 = new Title();
        title2.setTitleId(titleId2);
        title2.setName(titleName2);
        title2.setPlatform(titlePlatform2);

        // Mock responses
        when(xboxTitleDataService.searchTitles(userInput)).thenReturn(Arrays.asList(title1, title2));

        // Create instance of class to be tested
        XboxTitleService xboxTitleService = new XboxTitleService(xboxTitleDataService);

        // Call method to be tested
        Map<String, String> result = xboxTitleService.searchGames(userInput);

        // Create expected result
        Map<String, String> expectedResult = new HashMap<>();
        expectedResult.put(String.format("%s <strong>(%s)</strong>", title1.getName(), titlePlatform1), String.format("/game%s", titleId1));
        expectedResult.put(String.format("%s <strong>(%s)</strong>", title2.getName(), titlePlatform2), String.format("/game%s", titleId2));

        // Assert result
        assertEquals(expectedResult, result);
    }
}