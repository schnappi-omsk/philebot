package com.gundomrays.philebot.telegram.bot;

import com.gundomrays.philebot.xbox.domain.Profile;
import com.gundomrays.philebot.xbox.xapi.XBoxUserRegistrationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Chat;

import java.util.List;
import java.util.Random;

@Service
public class PeriodicalMessageService {

    @Value("${messages.periodical}")
    private String message;

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

        return String.format(message, wrapLink(makePingUrl(String.valueOf(user.getTgId())), "@" + user.getTgUsername()));
    }

    private String makePingUrl(final String username) {
        return String.format("tg://user?id=%s", username);
    }

    private String wrapLink(final String url, final String text) {
        return String.format("<a href='%s'>%s</a>", url, text);
    }

}
