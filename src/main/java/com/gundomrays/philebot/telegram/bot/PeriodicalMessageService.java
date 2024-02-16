package com.gundomrays.philebot.telegram.bot;

import com.gundomrays.philebot.telegram.util.TelegramChatUtils;
import com.gundomrays.philebot.xbox.domain.Profile;
import com.gundomrays.philebot.xbox.xapi.XBoxUserRegistrationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class PeriodicalMessageService {

    @Value("${messages.periodical.text}")
    private String message;

    @Value("${messages.periodical.probability}")
    private Integer probability;

    private final XBoxUserRegistrationService xBoxUserRegistrationService;

    public PeriodicalMessageService(XBoxUserRegistrationService xBoxUserRegistrationService) {
        this.xBoxUserRegistrationService = xBoxUserRegistrationService;
    }

    public String message() {
        List<Profile> users = xBoxUserRegistrationService.registeredUsers();

        if (users.isEmpty()) {
            return null;
        }

        final Random random = new Random();
        final Profile user = users.get(random.nextInt(users.size()));

        String userPingUrl = TelegramChatUtils.makePingUrl(String.valueOf(user.getTgId()));
        String userLink = TelegramChatUtils.wrapLink(userPingUrl, "@" + user.getTgUsername());
        return String.format(message, userLink);
    }

    public Integer probability() {
        return probability;
    }

}
