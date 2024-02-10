package com.gundomrays.philebot.command;

import com.gundomrays.philebot.data.XboxTitleHistoryDataService;
import com.gundomrays.philebot.xbox.domain.Title;
import com.gundomrays.philebot.xbox.xapi.XboxTitleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PhilGameStatsCommandTest {

    @Mock
    private XboxTitleHistoryDataService xboxTitleHistoryDataService;

    @Mock
    private XboxTitleService xboxTitleService;

    @InjectMocks
    private PhilGameStatsCommand philGameStatsCommand;

    @Test
    public void testExecute() {
        // Set up test case
        String argument = "testArgument";

        CommandRequest request = new CommandRequest();
        request.setArgument(argument);

        Title title = new Title();
        title.setName(argument);
        title.setTitleImg("testImg");

        Map<String, Integer> titleStats = new HashMap<>();
        titleStats.put("testPlayer", 1);
        titleStats.put("testPlayer2", 10);

        // Mock behaviours
        when(xboxTitleService.findTitle(any())).thenReturn(title);
        when(xboxTitleHistoryDataService.getTitleStatistics(any())).thenReturn(titleStats);

        // Perform test
        CommandResponse response = philGameStatsCommand.execute(request);

        // Verification
        assertNotNull(response);
        assertEquals("<strong>" + argument + "</strong>\n\n<code>" + "testPlayer2"
                + PhilCommandUtils.additionalSpaces("testPlayer2", "testPlayer2".length())
                + "\t:\t\t\t" + "10%" + "</code>\n" +
                "<code>testPlayer"
                + PhilCommandUtils.additionalSpaces("testPlayer", "testPlayer2".length())
                + "\t:\t\t\t" + "1%</code>".trim(), response.getMessage().trim());
        assertEquals("testImg", response.getMediaUrl());
    }
}