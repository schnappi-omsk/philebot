package com.gundomrays.philebot.command;

import com.gundomrays.philebot.data.XboxTitleHistoryDataService;
import com.gundomrays.philebot.xbox.domain.Gamerscore;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class PhilLeaderboardCommandTest {

    /**
     This class tests the execute method of PhilLeaderboardCommand class
     **/

    @Test
    void testExecute() {
        XboxTitleHistoryDataService mockService = Mockito.mock(XboxTitleHistoryDataService.class);
        PhilLeaderboardCommand command = new PhilLeaderboardCommand(mockService);

        Gamerscore testGamerscore1 = new Gamerscore();
        testGamerscore1.setGamertag("Test Gamertag 1");
        testGamerscore1.setScore(100L);
        Gamerscore testGamerscore2 = new Gamerscore();
        testGamerscore2.setGamertag("Test Gamertag 2");
        testGamerscore2.setScore(200L);
        when(mockService.leaderboard())
                .thenReturn(Arrays.asList(
                        testGamerscore1,
                        testGamerscore2));

        CommandRequest request = new CommandRequest();
        CommandResponse response = command.execute(request);

        String expectedResponse = "<strong>TOP OF THE CHAT</strong>\n\n<code>Test Gamertag 1\t\t:\t\t100</code>\n" +
                                "<code>Test Gamertag 2\t\t:\t\t200</code>\n";
        assertEquals(expectedResponse, response.getMessage());
    }

    @Test
    void testExecuteWithException() {
        XboxTitleHistoryDataService mockService = Mockito.mock(XboxTitleHistoryDataService.class);
        PhilLeaderboardCommand command = new PhilLeaderboardCommand(mockService);

        when(mockService.leaderboard()).thenThrow(new RuntimeException("Test Exception"));

        CommandRequest request = new CommandRequest();
        CommandResponse response = command.execute(request);

        assertEquals("<code>Cannot retrieve leaderboard</code>", response.getMessage());
    }
}