package com.gundomrays.philebot.command;

import com.gundomrays.philebot.data.XboxTitleDataService;
import com.gundomrays.philebot.xbox.domain.Title;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service("/last")
public class PhilLastCommand implements PhilCommand {

    private static final String GAME_CMD = "/game";

    private final ApplicationContext applicationContext;

    private final XboxTitleDataService xboxTitleDataService;

    public PhilLastCommand(
            ApplicationContext applicationContext,
            XboxTitleDataService xboxTitleDataService
    ) {
        this.applicationContext = applicationContext;
        this.xboxTitleDataService = xboxTitleDataService;
    }

    @Override
    public CommandResponse execute(CommandRequest request) {
        final Title lastPlayedTitle = xboxTitleDataService.lastPlayedTitle();

        if (lastPlayedTitle == null) {
            CommandResponse response = new CommandResponse();
            response.setMessage("<code>Nothing played or something wrong.</code>");
            return response;
        }

        //TODO think about a way to avoid using appCtx
        final PhilCommand nextCommand = applicationContext.getBean(GAME_CMD, PhilCommand.class);
        final CommandRequest nextRequest = new CommandRequest();
        nextRequest.setCommand(GAME_CMD);
        nextRequest.setArgument(lastPlayedTitle.getTitleId());

        return nextCommand.execute(nextRequest);
    }

    private String nextCommandName(final Title title) {
        return String.format("%s%s", GAME_CMD, title.getTitleId());
    }
}
