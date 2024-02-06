package com.gundomrays.philebot.command;

import com.gundomrays.philebot.xbox.domain.Profile;
import com.gundomrays.philebot.xbox.xapi.XBoxUserRegistrationService;
import org.springframework.stereotype.Service;

@Service("/ping")
public class PhilTogglePingCommand implements PhilCommand {

    private static final String ON = "on";
    private static final String OFF = "off";

    private final XBoxUserRegistrationService xBoxUserRegistrationService;

    public PhilTogglePingCommand(XBoxUserRegistrationService xBoxUserRegistrationService) {
        this.xBoxUserRegistrationService = xBoxUserRegistrationService;
    }

    @Override
    public CommandResponse execute(CommandRequest request) {
        final String arg = request.getArgument();
        final CommandResponse response = new CommandResponse();

        if (arg == null || arg.isEmpty() || arg.isBlank()) {
            final Profile gamer = xBoxUserRegistrationService.retrieveUserProfileByTg(request.getCaller());
            if (gamer == null) {
                response.setMessage(String.format("<code>No profile registered for user @%s</code>", request.getCaller()));
            } else {
                response.setMessage(String.format("<code>Your ping setting is %s</code>", fromBoolean(gamer.isPing())));
            }
            return response;
        }

        if (!isValidArg(arg)) {
            response.setMessage("<code>Invalid argument. Format: /ping ON|OFF</code>");
            return response;
        }

        xBoxUserRegistrationService.togglePing(request.getCaller(), argToBoolean(arg));
        response.setMessage(String.format("<code>Ping is set to %s</code>", arg));

        return response;
    }

    private String fromBoolean(final boolean arg) {
        return arg ? ON : OFF;
    }

    private boolean argToBoolean(final String arg) {
        return ON.equalsIgnoreCase(arg);
    }

    private boolean isValidArg(final String arg) {
        return arg.equalsIgnoreCase(ON) || arg.equalsIgnoreCase(OFF);
    }
}
