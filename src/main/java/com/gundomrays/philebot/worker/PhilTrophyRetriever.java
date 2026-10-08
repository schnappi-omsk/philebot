package com.gundomrays.philebot.worker;

import com.gundomrays.philebot.messaging.MessageQueue;
import com.gundomrays.philebot.psn.PsnTrophyQueue;
import com.gundomrays.philebot.psn.domain.PsnProfile;
import com.gundomrays.philebot.psn.domain.PsnTrophy;
import com.gundomrays.philebot.telegram.util.TelegramChatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class PhilTrophyRetriever {

    private static final Logger log = LoggerFactory.getLogger(PhilTrophyRetriever.class);

    @Value("${ebot.serviceHost}")
    private String serviceHost;

    private final PsnTrophyQueue psnTrophyQueue;

    private final MessageQueue messageQueue;

    public PhilTrophyRetriever(PsnTrophyQueue psnTrophyQueue, MessageQueue messageQueue) {
        this.psnTrophyQueue = psnTrophyQueue;
        this.messageQueue = messageQueue;
    }

    @Scheduled(fixedDelay = 1L, timeUnit = TimeUnit.MINUTES)
    public void retrieve() {
        PsnTrophy trophy;
        do {
            trophy = psnTrophyQueue.takeTrophy();
            if (trophy != null) {
                String trophyMessage = playerTrophyText(trophy);
                log.info("Sending PSN trophy to chat: {}", trophyMessage);
                messageQueue.messageToSend(trophyMessage);
            }
        } while (trophy != null);
    }

    String playerTrophyText(final PsnTrophy trophy) {
        PsnProfile gamer = trophy.getProfile();
        final String userPingLink = gamer.isPing()
                ? TelegramChatUtils.wrapLink(TelegramChatUtils.makePingUrl(String.valueOf(gamer.getTgId())), "@" + gamer.getTgUsername())
                : String.format("<code>%s</code>", gamer.getTgUsername());
        return userPingLink + " — " + trophy.getType().getEmoji() + " "
                + TelegramChatUtils.wrapLink(trophyCardUrl(trophy), HtmlUtils.htmlEscape(trophy.getTitleName()));
    }

    private String trophyCardUrl(final PsnTrophy trophy) {
        return String.format(
                "%s/psn/card?name=%s&desc=%s&type=%s&rarity=%s&img=%s&seed=%s",
                serviceHost,
                encode(trophy.getTrophyName()),
                encode(trophy.getTrophyDetail()),
                encode(trophy.getType().getDisplayName()),
                encode(trophy.getEarnedRate()),
                encode(trophy.getTrophyIconUrl()),
                UUID.randomUUID()
        );
    }

    private static String encode(final String value) {
        return URLEncoder.encode(value != null ? value : "", StandardCharsets.UTF_8);
    }

}
