package com.gundomrays.philebot.command;

import com.gundomrays.philebot.data.HelpDataService;
import com.gundomrays.philebot.xbox.domain.Help;
import org.checkerframework.checker.units.qual.C;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.StringJoiner;

@Service("/help")
public class PhilHelpCommand implements PhilCommand {

    private final HelpDataService helpDataService;

    public PhilHelpCommand(HelpDataService helpDataService) {
        this.helpDataService = helpDataService;
    }

    @Override
    public CommandResponse execute(CommandRequest request) {
        final CommandResponse response = new CommandResponse();
        response.setMessage(helpText());
        return response;
    }

    private String helpText() {
        final Collection<Help> help = helpDataService.help();

        StringJoiner joiner = new StringJoiner("\n");
        help.forEach(h -> joiner.add(commandHelp(h)));
        return joiner.toString();

    }

    private String commandHelp(final Help command) {
        return String.format("<code>%s: %s</code>", command.getCommandId(), command.getCommandManual());
    }

}
