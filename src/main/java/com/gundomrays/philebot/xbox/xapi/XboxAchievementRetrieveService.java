package com.gundomrays.philebot.xbox.xapi;

import com.gundomrays.philebot.xbox.domain.ActivityItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@EnableScheduling
public class XboxAchievementRetrieveService {

    private static final Logger log = LoggerFactory.getLogger(XboxAchievementRetrieveService.class);

    private final XboxUserActivityService activityService;

    public XboxAchievementRetrieveService(XboxUserActivityService activityService) {
        this.activityService = activityService;
    }

    @Scheduled(fixedDelay = 5L, timeUnit = TimeUnit.MINUTES)
    public void newAchievements() {
        log.info("Start -- get new achievements");
        List<ActivityItem> achievements = activityService.allPlayersLatestAchievements();

        for (ActivityItem achievement : achievements) {
            log.info("{}: {} - {} ({}%) {} pts.", achievement.getContentTitle(), achievement.getAchievementName(),
                    achievement.getAchievementDescription(), achievement.getRarityPercentage(), achievement.getGamerscore());
        }

    }

}
