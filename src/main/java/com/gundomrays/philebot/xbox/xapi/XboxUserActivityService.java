package com.gundomrays.philebot.xbox.xapi;

import com.gundomrays.philebot.data.XboxProfileRepository;
import com.gundomrays.philebot.data.XboxTitleHistoryDataService;
import com.gundomrays.philebot.xbox.domain.*;
import com.gundomrays.philebot.xbox.xapi.executor.RateLimitedExecutor;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class XboxUserActivityService {

    private static final Logger log = LoggerFactory.getLogger(XboxUserActivityService.class);

    @Value("${ebot.limitPerUser:5}")
    private Integer limitPerUser;

    private final XApiClient xApiClient;

    private final XboxProfileRepository xboxProfileRepository;

    private final XboxTitleHistoryDataService xboxTitleHistoryDataService;

    private final RateLimitedExecutor rateLimitedExecutor;

    public XboxUserActivityService(XApiClient xApiClient,
                                   XboxProfileRepository xboxProfileRepository,
                                   XboxTitleHistoryDataService xboxTitleHistoryDataService,
                                   RateLimitedExecutor rateLimitedExecutor
    ) {
        this.xApiClient = xApiClient;
        this.xboxProfileRepository = xboxProfileRepository;
        this.xboxTitleHistoryDataService = xboxTitleHistoryDataService;
        this.rateLimitedExecutor = rateLimitedExecutor;
    }

    public List<ActivityItem> allPlayersLatestAchievements() {
        final List<ActivityItem> activities = new ArrayList<>();
        Iterable<Profile> players = xboxProfileRepository.findAll();

        for (Profile player : players) {
            log.info("Retrieving activity of: {}", player.getGamertag());
            final LocalDateTime lastAchievement = player.getLastAchievement();
            final Callable<Activity> activityTask = () -> playerActivity(player);
            CompletableFuture<Activity> activityFuture = rateLimitedExecutor.submit(2, activityTask);
            try {
                Activity playerActivity = activityFuture.get();
                playerActivity.getActivityItems().stream()
                        .filter(item -> item.getDate().isAfter(lastAchievement))
                        .sorted(Comparator.reverseOrder())
                        .limit(limitPerUser)
                        .forEach(activities::add);
            } catch (InterruptedException | ExecutionException e) {
                log.error("Cannot retrieve player activity: " + player.getGamertag(), e);
                throw new RuntimeException("Cannot retrieve player activity: " + player.getGamertag(), e);
            }
        }

        return activities;
    }

    public Activity playerActivity(final Profile xboxProfile) {
        Objects.requireNonNull(xboxProfile);

        final Activity playerActivity = xApiClient.userActivity(xboxProfile.getId());
        final TitleHistoryCache cache = new TitleHistoryCache();

        playerActivity.getActivityItems().stream()
                .max(Comparator.naturalOrder())
                .filter(item -> item.getDate().isAfter(xboxProfile.getLastAchievement()))
                .ifPresent(item -> {
                    TitleHistory titleHistory = cache.getTitleHistory();
                    if (titleHistory == null || cache.needToUpdate(item.getTitleId())) {
                        log.info("Need to update internal cache with title history for xuid={} and title={}",
                                xboxProfile.getId(), item.getContentTitle());
                        titleHistory = xboxTitleHistoryDataService.findTitleHistory(xboxProfile.getId(), item);

                        if (titleHistory == null) {
                            log.info("No title history for xuid={} and title={}, need to update from XAPI",
                                    xboxProfile.getId(),item.getContentTitle());
                            final TitleHistory updatedFromXapi = xApiClient.titleHistory(xboxProfile.getId());
                            final Title title = updatedFromXapi.getTitles().stream()
                                    .filter(t -> t.getTitleId().equals(item.getTitleId()))
                                    .findFirst()
                                    .orElseThrow(() -> new RuntimeException("Title is null in TitleHistory response!"));
                            titleHistory = xboxTitleHistoryDataService.saveTitleHistory(xboxProfile, title);
                        }
                        cache.setTitleHistory(titleHistory);
                    }

                    xboxTitleHistoryDataService.updateTitleHistory(titleHistory, item);

                    xboxProfile.setLastAchievement(item.getDate());
                    xboxProfileRepository.save(xboxProfile);
                    log.info("User's {} last activity was updated", xboxProfile.getGamertag());
                });

        return playerActivity;
    }

    @Setter
    @Getter
    private static class TitleHistoryCache {
        private TitleHistory titleHistory;

        boolean needToUpdate(final String titleId) {
            Objects.requireNonNull(titleId);

            return titleHistory == null || !titleId.equals(titleHistory.getTitle().getTitleId());
        }
    }

}
