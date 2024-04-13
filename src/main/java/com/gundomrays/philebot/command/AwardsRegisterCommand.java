package com.gundomrays.philebot.command;

import com.gundomrays.philebot.telegram.bot.awards.ChatAwardsService;
import com.gundomrays.philebot.telegram.util.TelegramChatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("/pidoreg")
public class AwardsRegisterCommand implements PhilCommand {

    private final static Logger log = LoggerFactory.getLogger(AwardsRegisterCommand.class);

    private final ChatAwardsService chatAwardsService;

    public AwardsRegisterCommand(ChatAwardsService chatAwardsService) {
        this.chatAwardsService = chatAwardsService;
    }

    @Override
    public CommandResponse execute(CommandRequest request) {
        final String tgId = String.valueOf(request.getCallerId());
        final String tgName = request.getCaller();
        final String callerLink = TelegramChatUtils.createUserLink(tgId, tgName);

        CommandResponse response = new CommandResponse();
        try {
            if (chatAwardsService.registerNominee(tgId, tgName)) {
                response.setMessage(String.format("Поздравляю, %s, ты теперь участвуешь в самой престижной премии!", callerLink));
            } else {
                response.setMessage(String.format("Эй, %s, ты уже номинант, остановись!", callerLink));
            }
        } catch (Exception e) {
            response.setMessage("Какая-то ошибка. Надо бы логи посмотреть");
            log.error(e.getMessage(), e);
        }
        return response;
    }
}
