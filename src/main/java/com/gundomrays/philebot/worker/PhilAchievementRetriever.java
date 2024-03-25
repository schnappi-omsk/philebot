package com.gundomrays.philebot.worker;

import com.gundomrays.philebot.messaging.MessageQueue;
import com.gundomrays.philebot.telegram.util.TelegramChatUtils;
import com.gundomrays.philebot.xbox.domain.*;
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

    private final AchievementQueue achievementQueue;

    private final MessageQueue messageQueue;

    public PhilAchievementRetriever(AchievementQueue achievementQueue,
                                    MessageQueue messageQueue) {
        this.achievementQueue = achievementQueue;
        this.messageQueue = messageQueue;
    }

    @Scheduled(fixedDelay = 1L, timeUnit = TimeUnit.MINUTES)
    public void retrieve() {

        XboxAchievement xboxAchievement;
        do {
            xboxAchievement = achievementQueue.takePlayerAchievement();
            if (xboxAchievement != null) {
                String achievementMessage = playerAchievementText(xboxAchievement);
                log.info("Sending achievement to chat: {}", achievementMessage);
                messageQueue.messageToSend(achievementMessage);
            }
        } while (xboxAchievement != null);
    }

    private String playerAchievementText(final XboxAchievement achievement) {
        Profile gamer = achievement.getProfile();
        final String userPingLink = gamer.isPing()
                ? TelegramChatUtils.wrapLink(TelegramChatUtils.makePingUrl(String.valueOf(gamer.getTgId())), "@" + gamer.getTgUsername())
                : String.format("<code>%s</code>", gamer.getTgUsername());
        return userPingLink + " — " + playerAchievementUrl(achievement);
    }

    private String playerAchievementUrl(final XboxAchievement xboxAchievement) {
        TitleHubAchievement achievement = xboxAchievement.getAchievement();
        String gamerscore = achievement.getRewards()
                .stream()
                .filter(r -> "Gamerscore".equalsIgnoreCase(r.getType()))
                .map(Rewards::getValue)
                .findFirst()
                .orElse("0");
        String achievementIconUrl = achievement.getMediaAssets()
                .stream()
                .filter(ass -> "Icon".equalsIgnoreCase(ass.getType()))
                .map(MediaAsset::getUrl)
                .findFirst()
                .orElse("");
        String achievementUrl = String.format(
                "%s/xbox/%s/%s/%d/%s?imgUrl=%s&seed=%s",
                serviceHost,
                URLEncoder.encode(achievement.getName(), StandardCharsets.UTF_8),
                URLEncoder.encode(achievement.getDescription(), StandardCharsets.UTF_8),
                Integer.parseInt(gamerscore),
                achievement.getRarity().getCurrentPercentage(),
                URLEncoder.encode(achievementIconUrl, StandardCharsets.UTF_8),
                UUID.randomUUID()
        );
        String titleName = achievement.getTitleAssociations()
                .stream()
                .map(Title::getName)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Title is null. Weird"));
        return TelegramChatUtils.wrapLink(achievementUrl, titleName);
    }


}
