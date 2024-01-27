package com.gundomrays.philebot.command;

import com.gundomrays.philebot.data.XboxTitleDataService;
import com.gundomrays.philebot.data.XboxTitleHistoryDataService;
import com.gundomrays.philebot.xbox.domain.Title;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service("/game")
public class PhilGameStatsCommand implements PhilCommand {

    private final Logger log = LoggerFactory.getLogger(PhilGameStatsCommand.class);

    private final XboxTitleHistoryDataService xboxTitleHistoryDataService;

    private final XboxTitleDataService xboxTitleDataService;

    public PhilGameStatsCommand(XboxTitleHistoryDataService xboxTitleHistoryDataService,
                                XboxTitleDataService xboxTitleDataService) {
        this.xboxTitleHistoryDataService = xboxTitleHistoryDataService;
        this.xboxTitleDataService = xboxTitleDataService;
    }

    @Override
    public String execute(CommandRequest request) {
        final String titleId = request.getArgument();
        final Title title = xboxTitleDataService.titleById(titleId);

        if (title == null) {
            log.warn("Null returned for titleId={}", titleId);
            return String.format("Nothing found for titleId=%s", titleId);
        }

        log.info("Go for leaderboard for game: {}", title.getName());

        Map<Integer, String> titleStatistics = xboxTitleHistoryDataService.getTitleStatistics(title);
        log.info("{} gamers played {}", titleStatistics.size(), title.getName());

        return generateStatsMessage(title.getName(), titleStatistics);
    }

    private String generateStatsMessage(String titleName, Map<Integer, String> titleStats) {
        final StringBuilder builder = new StringBuilder();
        builder.append("<strong>").append(titleName).append("</strong>\n\n");

        final int longestPlayerNameLength = titleStats.values().stream()
                .mapToInt(String::length)
                .max()
                .orElse(0);

        titleStats.forEach((rate, player) -> builder.append("<code>")
                .append(player)
                .append(": ")
                .append(additionalSpaces(player, longestPlayerNameLength))
                .append("\t\t\t\t")
                .append(rate)
                .append("%")
                .append("</code>")
                .append("\n"));
        return builder.toString();
    }

    private String additionalSpaces(final String value, final int longest) {
        final int spacesCount = longest - value.length();
        return " ".repeat(spacesCount);
    }

}
