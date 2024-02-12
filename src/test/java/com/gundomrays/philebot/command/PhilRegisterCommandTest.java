package com.gundomrays.philebot.command;

import com.gundomrays.philebot.xbox.xapi.XBoxUserRegistrationService;
import com.gundomrays.philebot.xbox.xapi.XboxServiceResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

public class PhilRegisterCommandTest {
    @Test
    void testExecute() {
        //Initialize services and objects
        XBoxUserRegistrationService mockedRegistrationService = Mockito.mock(XBoxUserRegistrationService.class);
        PhilRegisterCommand command = new PhilRegisterCommand(mockedRegistrationService);

        //Initializing CommandRequest with dummy data
        CommandRequest request = new CommandRequest();
        request.setCaller("Test Caller");
        request.setCallerId(0L);
        request.setArgument("Test Argument");

        //Setup mocked service response
        XboxServiceResponse response = new XboxServiceResponse("Text");
        Mockito.when(mockedRegistrationService.registerUser(any(String.class), any(Long.class), any(String.class)))
                .thenReturn(response);

        String result = command.execute(request).getMessage();

        //Verification
        Mockito.verify(mockedRegistrationService, Mockito.times(1))
                .registerUser(request.getCaller(), request.getCallerId(), request.getArgument());

        //Assert
        assertEquals(String.format("<code>%s</code>", response.getText()), result, "<code>Result is not as expected</code>");
    }
}