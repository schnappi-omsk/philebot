package com.gundomrays.philebot.command;

import com.gundomrays.philebot.data.XboxTitleDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service("/find")
public class PhilFindGameCommand implements PhilCommand {

    private static final Logger log = LoggerFactory.getLogger(PhilFindGameCommand.class);

    private final XboxTitleDataService xboxTitleDataService;

    private final Map<String, PhilCommand> commandMap;

    public PhilFindGameCommand(XboxTitleDataService xboxTitleDataService,
                               Map<String, PhilCommand> commandMap) {
        this.xboxTitleDataService = xboxTitleDataService;
        this.commandMap = commandMap;
    }

    @Override
    public String execute(CommandRequest request) {
        String userInput = request.getArgument();
        log.info("Search game by user input={}", userInput);

        final Map<String, String> searchResults = xboxTitleDataService.searchTitles(userInput);
        log.info("{} games found by user input={}", searchResults.size(), userInput);

        if (searchResults.isEmpty()) {
            return "Igor tonet...";
        }

        if (searchResults.size() == 1) {
            final String[] commandParts = searchResults.values().iterator().next()
                    .split("(?<=\\D)(?=\\d)");

            if (commandParts.length == 2) {
                PhilCommand nextCommand = commandMap.get(commandParts[0]);
                return nextCommand.execute(requestForOneGame(commandParts[1]));
            }

        }

        return generateResultMessage(searchResults);
    }

    private CommandRequest requestForOneGame(String argument) {
        CommandRequest result = new CommandRequest();
        result.setArgument(argument);
        return result;
    }

    private String generateResultMessage(Map<String, String> searchResults) {
        final StringBuilder builder = new StringBuilder();
        searchResults.forEach((name, cmd) -> builder.append(name)
                .append(": ")
                .append(cmd)
                .append("\n"));
        return builder.toString();
    }

}

