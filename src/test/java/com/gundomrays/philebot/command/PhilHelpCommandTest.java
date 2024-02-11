package com.gundomrays.philebot.command;

import com.gundomrays.philebot.data.HelpDataService;
import com.gundomrays.philebot.xbox.domain.Help;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class PhilHelpCommandTest {

    @Test
    public void testExecute() {
        // Arrange
        Help help1 = new Help();
        help1.setCommandId("testCommandId1");
        help1.setCommandManual("testCommandManual1");

        Help help2 = new Help();
        help2.setCommandId("testCommandId2");
        help2.setCommandManual("testCommandManual2");

        HelpDataService helpDataService = mock(HelpDataService.class);
        when(helpDataService.help()).thenReturn(Arrays.asList(help1, help2));

        PhilHelpCommand philHelpCommand = new PhilHelpCommand(helpDataService);

        CommandRequest commandRequest = new CommandRequest();
        CommandResponse expectedResponse = new CommandResponse();
        String expectedMessage = "<code>" + help1.getCommandId() + ": " + help1.getCommandManual() + "</code>\n" +
                                 "<code>" + help2.getCommandId() + ": " + help2.getCommandManual() + "</code>";
        expectedResponse.setMessage(expectedMessage);

        // Act
        CommandResponse commandResponse = philHelpCommand.execute(commandRequest);

        // Assert
        assertEquals(expectedResponse.getMessage(), commandResponse.getMessage());
    }
}