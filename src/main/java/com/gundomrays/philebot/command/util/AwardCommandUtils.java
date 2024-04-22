package com.gundomrays.philebot.command.util;

import com.gundomrays.philebot.command.PhilCommandUtils;
import com.gundomrays.philebot.telegram.domain.AwardScore;

import java.util.List;

public class AwardCommandUtils {

    public static String awardLeaderboardText(List<AwardScore> leaderboard) {
        final StringBuilder leaderboardMsg = new StringBuilder();

        final int maxLen = leaderboard.stream()
                .mapToInt(l -> l.getNomineeName().length()).max()
                .orElse(0);

        leaderboard.forEach(record -> {
            leaderboardMsg.append("<code>")
                    .append(record.getNomineeName())
                    .append(PhilCommandUtils.additionalSpaces(record.getNomineeName(), maxLen))
                    .append("\t\t:\t\t")
                    .append(record.getAwardCount())
                    .append(declension(record.getAwardCount()))
                    .append("</code>")
                    .append("\n");
        });
        return leaderboardMsg.toString();
    }

    private static String declension(final Long value) {
        return switch ((int) (value % 10)) {
            case 2, 3, 4 -> value < 10 || value > 20 ? " раза" : " раз";
            default -> " раз";
        };
    }



}
