package com.gundomrays.philebot.command;

import com.gundomrays.philebot.data.XboxTitleDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service("/find")
public class PhilFindGameCommand implements PhilCommand {

    private static final Logger log = LoggerFactory.getLogger(PhilFindGameCommand.class);

    private final XboxTitleDataService xboxTitleDataService;

    public PhilFindGameCommand(XboxTitleDataService xboxTitleDataService) {
        this.xboxTitleDataService = xboxTitleDataService;
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

        return generateResultMessage(searchResults);
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

