package com.gundomrays.philebot.command;

import com.gundomrays.philebot.telegram.bot.clown.ClownService;
import com.gundomrays.philebot.telegram.bot.clown.domain.ClownRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("/woke")
public class ClownCommand implements PhilCommand {

    private final static Logger log = LoggerFactory.getLogger(ClownCommand.class);

    private final ClownService clownService;

    public ClownCommand(ClownService clownService) {
        this.clownService = clownService;
    }

    @Override
    public CommandResponse execute(CommandRequest request) {
        final String userInput = request.getArgument();
        log.info("Search woke game by user input={}", userInput);

        final List<ClownRecord> searchResults = clownService.searchGames(userInput);
        log.info("{} games found for input {}", searchResults.size(), userInput);

        if (searchResults.isEmpty()) {
            return PhilCommandUtils.textResponse("Нет повесточных игр по твоему запросу...");
        }

        return PhilCommandUtils.textResponse(generateResultMessage(searchResults));
    }

    private String generateResultMessage(final List<ClownRecord> searchResults) {
        final StringBuilder resultBuilder = new StringBuilder();

        searchResults.forEach(record -> resultBuilder.append("<strong>")
                .append(record.getName())
                .append("</strong>")
                .append(" — ")
                .append(record.getWoke().getComment())
                .append("<code>")
                .append(" (")
                .append(record.getDescription())
                .append(")")
                .append("</code>")
                .append("\n\n"));

        return resultBuilder.toString();
    }

}


