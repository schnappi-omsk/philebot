package com.gundomrays.philebot.telegram.bot;

import com.gundomrays.philebot.telegram.util.TelegramChatUtils;
import com.gundomrays.philebot.xbox.domain.Profile;
import com.gundomrays.philebot.xbox.xapi.XBoxUserRegistrationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class UserActivityService {

    @Value("${messages.activate}")
    private String activationMessage;

    @Value("${messages.deactivate}")
    private String deactivationMessage;

    private final XBoxUserRegistrationService xBoxUserRegistrationService;

    public UserActivityService(XBoxUserRegistrationService xBoxUserRegistrationService) {
        this.xBoxUserRegistrationService = xBoxUserRegistrationService;
    }

    public Collection<Profile> registeredUsers() {
        return xBoxUserRegistrationService.registeredUsers();
    }

    public String activationMessage(final Profile user) {
        final String userPingUrl = TelegramChatUtils.makePingUrl(String.valueOf(user.getTgId()));
        final String userLink = TelegramChatUtils.wrapLink(userPingUrl, "@" + user.getTgUsername());
        return String.format(activationMessage, userLink);
    }

    public String deactivationMessage(final Profile user) {
        final String userPingUrl = TelegramChatUtils.makePingUrl(String.valueOf(user.getTgId()));
        final String userLink = TelegramChatUtils.wrapLink(userPingUrl, "@" + user.getTgUsername());
        return String.format(deactivationMessage, userLink);
    }

    public void deactivateUser(final Profile user) {
        xBoxUserRegistrationService.deactivateUser(user);
    }

    public void activateUser(final Profile user) {
        xBoxUserRegistrationService.activateUser(user);
    }

}
