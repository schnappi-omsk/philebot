package com.gundomrays.philebot.command;

import com.gundomrays.philebot.xbox.xapi.XBoxUserRegistrationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class PhilTogglePingCommandTest {

    /**
     * Test class for PhilTogglePingCommand class.
     * This class encompasses unit tests for method execute in PhilTogglePingCommand.
     */

    // Object under test
    private PhilTogglePingCommand philTogglePingCommand;

    @Test
    void testExecuteWithValidArgOn() {
        final XBoxUserRegistrationService xBoxUserRegistrationService = Mockito.mock(XBoxUserRegistrationService.class);
        final CommandRequest request = new CommandRequest();
        request.setArgument("on");
        request.setCaller("user");

        philTogglePingCommand = new PhilTogglePingCommand(xBoxUserRegistrationService);
        final CommandResponse response = philTogglePingCommand.execute(request);

        verify(xBoxUserRegistrationService, times(1)).togglePing(anyString(), anyBoolean());
        assertEquals("<code>Ping is set to on</code>", response.getMessage());
    }

    @Test
    void testExecuteWithValidArgOff() {
        final XBoxUserRegistrationService xBoxUserRegistrationService = Mockito.mock(XBoxUserRegistrationService.class);
        final CommandRequest request = new CommandRequest();
        request.setArgument("off");
        request.setCaller("user");

        philTogglePingCommand = new PhilTogglePingCommand(xBoxUserRegistrationService);
        final CommandResponse response = philTogglePingCommand.execute(request);

        verify(xBoxUserRegistrationService, times(1)).togglePing(anyString(), anyBoolean());
        assertEquals("<code>Ping is set to off</code>", response.getMessage());
    }

    @Test
    void testExecuteWithInvalidArg() {
        final XBoxUserRegistrationService xBoxUserRegistrationService = Mockito.mock(XBoxUserRegistrationService.class);
        final CommandRequest request = new CommandRequest();
        request.setArgument("invalid");
        request.setCaller("user");

        philTogglePingCommand = new PhilTogglePingCommand(xBoxUserRegistrationService);
        final CommandResponse response = philTogglePingCommand.execute(request);

        verify(xBoxUserRegistrationService, times(0)).togglePing(anyString(), anyBoolean());
        assertEquals("<code>Invalid argument. Format: /ping ON|OFF</code>", response.getMessage());
    }

}