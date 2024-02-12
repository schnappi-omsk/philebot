package com.gundomrays.philebot.command;

import com.gundomrays.philebot.data.XboxTitleDataService;
import com.gundomrays.philebot.xbox.domain.Title;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


public class PhilLastCommandTest {

    /**
     * Testing the execute() method of the PhilLastCommand class.
     * This method should interact with the XboxTitleDataService to fetch the last played title,
     * and then execute a new command based on that title.
     */
    @Test
    public void testExecute_lastPlayedTitleIsNull() {
        // Mocking dependencies
        XboxTitleDataService xboxTitleDataService = Mockito.mock(XboxTitleDataService.class);
        PhilCommandService philCommandService = Mockito.mock(PhilCommandService.class);

        // Creating instance to test with mocked dependencies
        PhilLastCommand philLastCommand = new PhilLastCommand(xboxTitleDataService, philCommandService);

        // Mocking lastPlayedTitle method to return null
        Mockito.when(xboxTitleDataService.lastPlayedTitle()).thenReturn(null);

        // Testing the method
        CommandResponse response = philLastCommand.execute(new CommandRequest());

        Assertions.assertNotNull(response);
        Assertions.assertEquals("<code>Nothing played or something wrong.</code>", response.getMessage());
    }

    @Test
    public void testExecute_lastPlayedTitleExists() {
        // Mocking dependencies
        XboxTitleDataService xboxTitleDataService = Mockito.mock(XboxTitleDataService.class);
        PhilCommandService philCommandService = Mockito.mock(PhilCommandService.class);

        // Creating instance to test with mocked dependencies
        PhilLastCommand philLastCommand = new PhilLastCommand(xboxTitleDataService, philCommandService);

        // Mocking lastPlayedTitle method to return a title
        Title title = new Title();
        title.setTitleId("titleId");
        Mockito.when(xboxTitleDataService.lastPlayedTitle()).thenReturn(title);

        // Mocking the new command to return a specific response
        CommandResponse cmdResponse = new CommandResponse();
        cmdResponse.setMessage("<code>Some game played before</code>");
        PhilCommand nextCommand = Mockito.mock(PhilCommand.class);
        Mockito.when(philCommandService.command("/game")).thenReturn(nextCommand);
        Mockito.when(nextCommand.execute(Mockito.any(CommandRequest.class))).thenReturn(cmdResponse);

        // Testing the method
        CommandResponse response = philLastCommand.execute(new CommandRequest());

        Assertions.assertNotNull(response);
        Assertions.assertEquals("<code>Some game played before</code>", response.getMessage());
    }
}