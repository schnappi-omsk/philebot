package com.gundomrays.philebot.worker;

import com.gundomrays.philebot.xbox.domain.ActivityItem;
import com.gundomrays.philebot.xbox.domain.Profile;
import com.gundomrays.philebot.xbox.xapi.XBoxUserRegistrationService;
import com.gundomrays.philebot.xbox.xapi.XboxAchievementRetrieveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PhilAchievementRetriever {

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
        log.info(achievementUrl(username, item));
        return "<a href=\"" + achievementUrl(username, item) + "\"> </a>";
    }

    private String achievementUrl(final String username, final ActivityItem item) {
        return String.format(
                "http://localhost:8080/xbox/%s/%s/%s/%s/%d/%d?imgUrl=%s",
                username,
                item.getContentTitle(),
                item.getAchievementName(),
                item.getAchievementDescription(),
                item.getGamerscore(),
                item.getRarityPercentage(),
                item.getAchievementIcon()
        );
    }

}
