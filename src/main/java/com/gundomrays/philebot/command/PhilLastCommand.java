package com.gundomrays.philebot.command;

import com.gundomrays.philebot.data.XboxTitleDataService;
import com.gundomrays.philebot.xbox.domain.Title;
import org.springframework.stereotype.Service;

@Service("/last")
public class PhilLastCommand implements PhilCommand {

    private static final String GAME_CMD = "/game";

    private final XboxTitleDataService xboxTitleDataService;

    private final PhilCommandService philCommandService;

    public PhilLastCommand(
            XboxTitleDataService xboxTitleDataService,
            PhilCommandService philCommandService
    ) {
        this.xboxTitleDataService = xboxTitleDataService;
        this.philCommandService = philCommandService;
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
        final PhilCommand nextCommand = philCommandService.command(GAME_CMD);
        final CommandRequest nextRequest = new CommandRequest();
        nextRequest.setCommand(GAME_CMD);
        nextRequest.setArgument(lastPlayedTitle.getTitleId());

        return nextCommand.execute(nextRequest);
    }
}
