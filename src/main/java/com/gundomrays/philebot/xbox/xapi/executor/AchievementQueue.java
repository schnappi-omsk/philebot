package com.gundomrays.philebot.xbox.xapi.executor;

import com.gundomrays.philebot.xbox.domain.ActivityItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class AchievementQueue {

    private static final Logger log = LoggerFactory.getLogger(AchievementQueue.class);

    private final BlockingQueue<ActivityItem> queue = new LinkedBlockingQueue<>();

    public void placeAchievement(final ActivityItem achievement) {
        if (queue.offer(achievement)) {
            log.info("Achievement added to queue. Gamer: {}, game: {}, achievement: {}",
                    achievement.getUserXuid(), achievement.getContentTitle(), achievement.getAchievementName());
        } else {
            log.error("Cannot add achievement to queue. Gamer: {}, game: {}, achievement: {}",
                    achievement.getUserXuid(), achievement.getContentTitle(), achievement.getAchievementName());
        }

    }


    public ActivityItem takeAchievement() {
        final ActivityItem achievement = queue.poll();
        if (achievement != null) {
            log.info("Got achievement from queue. Gamer: {}, game: {}, achievement: {}",
                    achievement.getUserXuid(), achievement.getContentTitle(), achievement.getAchievementName());

        }
        return achievement;
    }

}
