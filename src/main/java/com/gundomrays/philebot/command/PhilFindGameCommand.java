package com.gundomrays.philebot.command;

import com.gundomrays.philebot.xbox.xapi.XboxTitleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service("/find")
public class PhilFindGameCommand implements PhilCommand {

    private static final Logger log = LoggerFactory.getLogger(PhilFindGameCommand.class);

    private final XboxTitleService xboxTitleService;

    private final PhilCommandService philCommandService;

    public PhilFindGameCommand(XboxTitleService xboxTitleService, PhilCommandService philCommandService) {
        this.xboxTitleService = xboxTitleService;
        this.philCommandService = philCommandService;
    }

    @Override
    public CommandResponse execute(CommandRequest request) {
        String userInput = request.getArgument();
        log.info("Search game by user input={}", userInput);

        final Map<String, String> searchResults = xboxTitleService.searchGames(userInput);
        log.info("{} games found by user input={}", searchResults.size(), userInput);

        if (searchResults.isEmpty()) {
            return PhilCommandUtils.textResponse("Igor tonet...");
        }

        if (searchResults.size() == 1) {
            final String[] commandParts = searchResults.values().iterator().next()
                    .split("(?<=\\D)(?=\\d)");

            if (commandParts.length == 2) {
                PhilCommand nextCommand = philCommandService.command(commandParts[0]);
                return nextCommand.execute(requestForOneGame(commandParts[1]));
            }

        }

        return PhilCommandUtils.textResponse(generateResultMessage(searchResults));
    }

    private CommandRequest requestForOneGame(String argument) {
        CommandRequest result = new CommandRequest();
        result.setArgument(argument);
        return result;
    }

    private String generateResultMessage(Map<String, String> searchResults) {
        final StringBuilder builder = new StringBuilder();
        searchResults.forEach((name, cmd) -> builder.append("<code>")
                .append(name)
                .append(": ")
                .append("</code>")
                .append(cmd)
                .append("\n"));
        return builder.toString();
    }

}

