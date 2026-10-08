package com.gundomrays.philebot.command;

import com.gundomrays.philebot.psn.PsnUserRegistrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

@Service("/psnreg")
public class PhilPsnRegisterCommand implements PhilCommand {

    private static final Logger log = LoggerFactory.getLogger(PhilPsnRegisterCommand.class);

    private final PsnUserRegistrationService registrationService;

    public PhilPsnRegisterCommand(PsnUserRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @Override
    public CommandResponse execute(CommandRequest request) {
        log.info("PSN registration request was received for Online ID = {}", request.getArgument());
        final String result = registrationService.registerUser(
                request.getCaller(),
                request.getCallerId(),
                request.getArgument()
        );
        return PhilCommandUtils.textResponse(String.format("<code>%s</code>", HtmlUtils.htmlEscape(result)));
    }
}
