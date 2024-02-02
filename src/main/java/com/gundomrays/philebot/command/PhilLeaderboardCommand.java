package com.gundomrays.philebot.command;

import com.gundomrays.philebot.data.XboxTitleHistoryDataService;
import com.gundomrays.philebot.xbox.domain.Gamerscore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("/top")
public class PhilLeaderboardCommand implements PhilCommand {

    private static Logger log = LoggerFactory.getLogger(PhilLeaderboardCommand.class);

    private final XboxTitleHistoryDataService xboxTitleHistoryDataService;

    public PhilLeaderboardCommand(XboxTitleHistoryDataService xboxTitleHistoryDataService) {
        this.xboxTitleHistoryDataService = xboxTitleHistoryDataService;
    }

    @Override
    public CommandResponse execute(CommandRequest request) {
        final CommandResponse response = new CommandResponse();
        try {
            response.setMessage(buildText(xboxTitleHistoryDataService.leaderboard()));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            response.setMessage(buildError());
        }
        return response;
    }

    private String buildError() {
        return "<code>Cannot retrieve leaderboard</code>";
    }

    private String buildText(final List<Gamerscore> leaderboard) {
        final StringBuilder builder = new StringBuilder("<strong>TOP OF THE CHAT</strong>");
        builder.append("\n\n");

        int maxNameLength = leaderboard.stream()
                .mapToInt(l -> l.getGamertag().length()).max()
                .orElse(0);

        for (final Gamerscore score : leaderboard) {
            builder.append("<code>")
                    .append(score.getGamertag())
                    .append(PhilCommandUtils.additionalSpaces(score.getGamertag(), maxNameLength))
                    .append("\t\t:\t\t")
                    .append(score.getScore())
                    .append("</code>")
                    .append("\n");
        }
        return builder.toString();
    }

}
