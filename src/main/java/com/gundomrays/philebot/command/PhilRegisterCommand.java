package com.gundomrays.philebot.command;

import com.gundomrays.philebot.xbox.xapi.XBoxUserRegistrationService;
import com.gundomrays.philebot.xbox.xapi.XboxServiceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("/reg")
public class PhilRegisterCommand implements PhilCommand {

    private static final Logger log = LoggerFactory.getLogger(PhilRegisterCommand.class);

    private final XBoxUserRegistrationService registrationService;

    public PhilRegisterCommand(XBoxUserRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @Override
    public CommandResponse execute(CommandRequest request) {
        log.info("Registration request was received for gamertag = {}", request.getArgument());
        XboxServiceResponse result = registrationService.registerUser(
                request.getCaller(),
                request.getCallerId(),
                request.getArgument()
        );
        return PhilCommandUtils.textResponse(result.getText());
    }
}