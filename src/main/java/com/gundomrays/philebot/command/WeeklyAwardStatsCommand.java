package com.gundomrays.philebot.command;

import com.gundomrays.philebot.command.util.AwardCommandUtils;
import com.gundomrays.philebot.telegram.data.ChatAwardsRepository;
import com.gundomrays.philebot.telegram.domain.AwardScore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("/weeklyPidor")
public class WeeklyAwardStatsCommand implements PhilCommand {

    private final ChatAwardsRepository chatAwardsRepository;

    public WeeklyAwardStatsCommand(ChatAwardsRepository chatAwardsRepository) {
        this.chatAwardsRepository = chatAwardsRepository;
    }
    @Override
    public CommandResponse execute(CommandRequest request) {
        List<AwardScore> awardScores = chatAwardsRepository.weeklyLeaderboard();
        final CommandResponse response = new CommandResponse();
        String leaderboardMessage = AwardCommandUtils.awardLeaderboardText(awardScores);
        String message = "<strong>Топ пидоров этой недели</strong>" +
                "\n\n" + leaderboardMessage;
        response.setMessage(message);

        return response;
    }

}
