package com.gundomrays.philebot.worker;

import com.gundomrays.philebot.messaging.MessageQueue;
import com.gundomrays.philebot.telegram.util.TelegramChatUtils;
import com.gundomrays.philebot.xbox.domain.ActivityItem;
import com.gundomrays.philebot.xbox.domain.Profile;
import com.gundomrays.philebot.xbox.xapi.XBoxUserRegistrationService;
import com.gundomrays.philebot.xbox.xapi.executor.AchievementQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class PhilAchievementRetriever {

    @Value("${ebot.serviceHost}")
    private String serviceHost;

    private static final Logger log = LoggerFactory.getLogger(PhilAchievementRetriever.class);

    private final XBoxUserRegistrationService xBoxUserRegistrationService;

    private final AchievementQueue achievementQueue;

    private final MessageQueue messageQueue;

    public PhilAchievementRetriever(XBoxUserRegistrationService xBoxUserRegistrationService,
                                    AchievementQueue achievementQueue,
                                    MessageQueue messageQueue) {
        this.xBoxUserRegistrationService = xBoxUserRegistrationService;
        this.achievementQueue = achievementQueue;
        this.messageQueue = messageQueue;
    }

    @Scheduled(fixedDelay = 1L, timeUnit = TimeUnit.MINUTES)
    public void retrieve() {
        ActivityItem achievement;
        do {
            achievement = achievementQueue.takeAchievement();
            if (achievement != null) {
                log.info("Got an achievement from the queue: {} : {} - {} ({}%) {} pts.",
                        achievement.getContentTitle(), achievement.getAchievementName(),
                        achievement.getAchievementDescription(), achievement.getRarityPercentage(), achievement.getGamerscore());

                String xuid = achievement.getUserXuid();
                final Profile gamer = xBoxUserRegistrationService.retrieveUserProfile(xuid);
                if (gamer == null) {
                    log.error("Cannot find gamer by xuid={}", xuid);
                    achievementQueue.placeAchievement(achievement);
                    continue;
                }
                messageQueue.messageToSend(achievementText(gamer, gamer.getTgId(), achievement));
            }

        } while (achievement != null);
    }

    private String achievementText(final Profile gamer, Long tgId, final ActivityItem item) {
        final String userPingLink = gamer.isPing()
                ? TelegramChatUtils.wrapLink(TelegramChatUtils.makePingUrl(String.valueOf(tgId)), "@" + gamer.getTgUsername())
                : String.format("<code>%s</code>", gamer.getTgUsername());
        final String text = userPingLink + " — " + TelegramChatUtils.wrapLink(achievementUrl(item), item.getContentTitle());
        log.info(text);
        return text;
    }

    private String achievementUrl(final ActivityItem item) {
        return String.format(
                "%s/xbox/%s/%s/%d/%d?imgUrl=%s&seed=%s",
                serviceHost,
                URLEncoder.encode(item.getAchievementName(), StandardCharsets.UTF_8),
                URLEncoder.encode(item.getAchievementDescription(), StandardCharsets.UTF_8),
                item.getGamerscore(),
                item.getRarityPercentage(),
                URLEncoder.encode(item.getAchievementIcon(), StandardCharsets.UTF_8),
                UUID.randomUUID()
        );
    }
}
