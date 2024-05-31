package com.gundomrays.philebot.command;

import com.gundomrays.philebot.command.util.AwardCommandUtils;
import com.gundomrays.philebot.telegram.data.ChatAwardsRepository;
import com.gundomrays.philebot.telegram.domain.AwardScore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("/pidorstats")
public class OverallAwardStatsCommand implements PhilCommand {

    private final ChatAwardsRepository chatAwardsRepository;

    public OverallAwardStatsCommand(ChatAwardsRepository chatAwardsRepository) {
        this.chatAwardsRepository = chatAwardsRepository;
    }

    @Override
    public CommandResponse execute(CommandRequest request) {
        List<AwardScore> awardScores = chatAwardsRepository.overallLeaderboard();
        final CommandResponse response = new CommandResponse();
        String leaderboardMessage = AwardCommandUtils.awardLeaderboardText(awardScores);
        String message = "<strong>Самые пидоры с начала времён</strong>" +
                "\n\n" + leaderboardMessage;
        response.setMessage(message);

        return response;
    }

}
