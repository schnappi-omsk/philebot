package com.gundomrays.philebot.xbox.xapi.executor;

import com.gundomrays.philebot.xbox.domain.XboxAchievement;
import com.gundomrays.philebot.xbox.domain.Profile;
import com.gundomrays.philebot.xbox.domain.TitleHubAchievement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class AchievementQueue {

    private static final Logger log = LoggerFactory.getLogger(AchievementQueue.class);

    private final BlockingQueue<XboxAchievement> titleHubQueue = new LinkedBlockingQueue<>();

    public void placeAchievement(final Profile gamer, final TitleHubAchievement achievement) {
        if (titleHubQueue.offer(new XboxAchievement(gamer, achievement))) {
            log.info("TitleHub achievement added to queue, player={}, achievement={}", gamer.getId(), achievement.getName());
        } else {
            log.error("Cannot add achievement to queue, player={}, achievement={}", gamer.getId(), achievement.getName());
        }
    }

    public XboxAchievement takePlayerAchievement() {
        final XboxAchievement achievement = titleHubQueue.poll();
        if (achievement != null) {
            log.info("Got achievement from queue. Gamer: {}, achievement: {}",
                    achievement.getProfile().getId(), achievement.getAchievement().getName());
        }
        return achievement;
    }


}
