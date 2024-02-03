package com.gundomrays.philebot.command;

import com.gundomrays.philebot.data.XboxTitleHistoryDataService;
import com.gundomrays.philebot.xbox.domain.Title;
import com.gundomrays.philebot.xbox.xapi.XboxTitleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service("/game")
public class PhilGameStatsCommand implements PhilCommand {

    private final Logger log = LoggerFactory.getLogger(PhilGameStatsCommand.class);

    private final XboxTitleHistoryDataService xboxTitleHistoryDataService;

    private final XboxTitleService xboxTitleService;

    public PhilGameStatsCommand(XboxTitleHistoryDataService xboxTitleHistoryDataService,
                                XboxTitleService xboxTitleService) {
        this.xboxTitleHistoryDataService = xboxTitleHistoryDataService;
        this.xboxTitleService = xboxTitleService;
    }

    @Override
    public CommandResponse execute(CommandRequest request) {
        String argument = request.getArgument();
        int pingIdx = argument.indexOf('@');
        final String titleId = pingIdx > -1 ? argument.substring(0, pingIdx) : argument;
        final Title title = xboxTitleService.findTitle(titleId);

        if (title == null) {
            log.warn("Null returned for titleId={}", titleId);
            return PhilCommandUtils.textResponse(String.format("<code>Nothing found for game %s</code>", titleId));
        }

        log.info("Go for leaderboard for game: {}", title.getName());

        Map<Integer, String> titleStatistics = xboxTitleHistoryDataService.getTitleStatistics(title);
        log.info("{} gamers played {}", titleStatistics.size(), title.getName());

        return PhilCommandUtils.captionedPhotoResponse(
                generateStatsMessage(title.getName(), titleStatistics), title.getTitleImg());
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
                .append(PhilCommandUtils.additionalSpaces(player, longestPlayerNameLength))
                .append("\t:\t\t\t")
                .append(rate)
                .append("%")
                .append("</code>")
                .append("\n"));
        return builder.toString();
    }

}
