package com.gundomrays.philebot.worker;

import com.gundomrays.philebot.xbox.domain.ActivityItem;
import com.gundomrays.philebot.xbox.domain.Profile;
import com.gundomrays.philebot.xbox.xapi.XBoxUserRegistrationService;
import com.gundomrays.philebot.xbox.xapi.XboxAchievementRetrieveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PhilAchievementRetriever {

    @Value("${ebot.serviceHost}")
    private String serviceHost;

    private static final Logger log = LoggerFactory.getLogger(PhilAchievementRetriever.class);

    private final XboxAchievementRetrieveService xboxAchievementRetrieveService;

    private final XBoxUserRegistrationService xBoxUserRegistrationService;

    public PhilAchievementRetriever(XboxAchievementRetrieveService xboxAchievementRetrieveService,
                                    XBoxUserRegistrationService xBoxUserRegistrationService) {
        this.xboxAchievementRetrieveService = xboxAchievementRetrieveService;
        this.xBoxUserRegistrationService = xBoxUserRegistrationService;
    }

    public Collection<String> retrieve() {
        final Collection<ActivityItem> activityItems = xboxAchievementRetrieveService.newAchievements();
        final Map<String, List<ActivityItem>> achievementsByXuid = activityItems.stream()
                .collect(Collectors.groupingBy(ActivityItem::getUserXuid));
        final Set<String> result = new HashSet<>();

        for (final String xuid : achievementsByXuid.keySet()) {
            final Profile gamer = xBoxUserRegistrationService.retrieveUserProfile(xuid);
            if (gamer == null) {
                log.error("Cannot find gamer by xuid={}", xuid);
                continue;
            }

            List<ActivityItem> achievements = achievementsByXuid.get(xuid);
            result.addAll(achievements.stream()
                            .map(ach -> achievementText(gamer.getTgUsername(), ach))
                            .collect(Collectors.toSet()));
        }

        return result;
    }

    private String achievementText(final String username, final ActivityItem item) {
        String text = wrapLink(makePingUrl(username), username) + " - " + wrapLink(achievementUrl(item), item.getContentTitle());
        log.info(text);
        return text;
    }

    private String achievementUrl(final ActivityItem item) {
        return String.format(
                "%s/xbox/%s/%s/%s/%d/%d?imgUrl=%s",
                serviceHost,
                URLEncoder.encode(item.getContentTitle(), StandardCharsets.UTF_8),
                URLEncoder.encode(item.getAchievementName(), StandardCharsets.UTF_8),
                URLEncoder.encode(item.getAchievementDescription(), StandardCharsets.UTF_8),
                item.getGamerscore(),
                item.getRarityPercentage(),
                item.getAchievementIcon()
        );
    }

    private String makePingUrl(final String username) {
        return String.format("tg://user?id=%s", "@" + username);
    }

    private String wrapLink(final String url, final String text) {
        return String.format("<a href='%s'>%s</a>", url, text);
    }

}
