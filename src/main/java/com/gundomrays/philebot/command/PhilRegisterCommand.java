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
    public String execute(String gamertag) {
        log.info("Registration request was received for gamertag = {}", gamertag);
        XboxServiceResponse result = registrationService.registerUser(gamertag);
        return result.getText();
    }

}