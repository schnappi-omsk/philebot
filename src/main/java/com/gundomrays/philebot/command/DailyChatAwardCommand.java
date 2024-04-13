package com.gundomrays.philebot.command;

import com.gundomrays.philebot.telegram.bot.awards.ChatAwardsService;
import com.gundomrays.philebot.telegram.domain.AwardNominee;
import com.gundomrays.philebot.telegram.util.TelegramChatUtils;
import org.springframework.stereotype.Service;

@Service("/pidor")
public class DailyChatAwardCommand implements PhilCommand {

    private final ChatAwardsService chatAwardsService;

    public DailyChatAwardCommand(ChatAwardsService chatAwardsService) {
        this.chatAwardsService = chatAwardsService;
    }

    @Override
    public CommandResponse execute(CommandRequest request) {
        final CommandResponse response = new CommandResponse();
        if (chatAwardsService.hasWinner()) {
            final AwardNominee winner = chatAwardsService.todayWinner();
            final String winnerLink = TelegramChatUtils.createUserLink(winner.getTgId(), winner.getTgName());
            response.setMessage(String.format("Сегодня уже есть пидор дня, и это %s", winnerLink));
        } else {
            final AwardNominee winner = chatAwardsService.awardGoesTo();
            final String winnerLink = TelegramChatUtils.createUserLink(winner.getTgId(), winner.getTgName());
            response.setMessage(String.format(chatAwardsService.congratulations(), winnerLink));
        }
        return response;
    }
}
