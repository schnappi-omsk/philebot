package com.gundomrays.philebot.telegram.bot;

import com.gundomrays.philebot.data.PsnTrophyDataService;
import com.gundomrays.philebot.psn.domain.PsnProfile;
import com.gundomrays.philebot.telegram.util.TelegramChatUtils;
import com.gundomrays.philebot.xbox.domain.Profile;
import com.gundomrays.philebot.xbox.xapi.XBoxUserRegistrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class UserActivityService {

    private static final Logger log = LoggerFactory.getLogger(UserActivityService.class);

    @Value("${messages.activate}")
    private String activationMessage;

    @Value("${messages.deactivate}")
    private String deactivationMessage;

    private final XBoxUserRegistrationService xBoxUserRegistrationService;

    private final PsnTrophyDataService psnTrophyDataService;

    public UserActivityService(XBoxUserRegistrationService xBoxUserRegistrationService,
                               PsnTrophyDataService psnTrophyDataService) {
        this.xBoxUserRegistrationService = xBoxUserRegistrationService;
        this.psnTrophyDataService = psnTrophyDataService;
    }

    public Collection<Profile> registeredUsers() {
        return xBoxUserRegistrationService.registeredUsers();
    }

    public String activationMessage(final Profile user) {
        return activationMessage(user.getTgId(), user.getTgUsername());
    }

    public String deactivationMessage(final Profile user) {
        return deactivationMessage(user.getTgId(), user.getTgUsername());
    }

    public void deactivateUser(final Profile user) {
        xBoxUserRegistrationService.deactivateUser(user);
    }

    public void activateUser(final Profile user) {
        xBoxUserRegistrationService.activateUser(user);
    }

    public Collection<PsnProfile> registeredPsnUsers() {
        return psnTrophyDataService.profiles();
    }

    public String activationMessage(final PsnProfile user) {
        return activationMessage(user.getTgId(), user.getTgUsername());
    }

    public String deactivationMessage(final PsnProfile user) {
        return deactivationMessage(user.getTgId(), user.getTgUsername());
    }

    public void deactivatePsnUser(final PsnProfile user) {
        user.setActive(false);
        psnTrophyDataService.saveProfile(user);
        log.warn("PSN user {} was deactivated", user.getTgUsername());
    }

    public void activatePsnUser(final PsnProfile user) {
        user.setActive(true);
        psnTrophyDataService.saveProfile(user);
        log.warn("PSN user {} was activated", user.getTgUsername());
    }

    private String activationMessage(final Long tgId, final String tgUsername) {
        final String userPingUrl = TelegramChatUtils.makePingUrl(String.valueOf(tgId));
        final String userLink = TelegramChatUtils.wrapLink(userPingUrl, "@" + tgUsername);
        return String.format(activationMessage, userLink);
    }

    private String deactivationMessage(final Long tgId, final String tgUsername) {
        final String userPingUrl = TelegramChatUtils.makePingUrl(String.valueOf(tgId));
        final String userLink = TelegramChatUtils.wrapLink(userPingUrl, "@" + tgUsername);
        return String.format(deactivationMessage, userLink);
    }

}
