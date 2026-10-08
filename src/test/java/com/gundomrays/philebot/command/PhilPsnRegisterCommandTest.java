package com.gundomrays.philebot.command;

import com.gundomrays.philebot.psn.PsnUserRegistrationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PhilPsnRegisterCommandTest {

    @Test
    void testExecute() {
        PsnUserRegistrationService mockedRegistrationService = Mockito.mock(PsnUserRegistrationService.class);
        PhilPsnRegisterCommand command = new PhilPsnRegisterCommand(mockedRegistrationService);

        CommandRequest request = new CommandRequest();
        request.setCaller("tg_player");
        request.setCallerId(42L);
        request.setArgument("<player>");

        Mockito.when(mockedRegistrationService.registerUser("tg_player", 42L, "<player>"))
                .thenReturn("PSN account <player> was not found");

        String result = command.execute(request).getMessage();

        Mockito.verify(mockedRegistrationService).registerUser("tg_player", 42L, "<player>");
        assertEquals("<code>PSN account &lt;player&gt; was not found</code>", result);
    }
}
