package com.gundomrays.philebot.xbox.xapi;

import com.gundomrays.philebot.data.XboxProfileRepository;
import com.gundomrays.philebot.data.XboxTitleHistoryDataService;
import com.gundomrays.philebot.xbox.domain.*;
import com.gundomrays.philebot.xbox.xapi.executor.AchievementQueue;
import com.gundomrays.philebot.xbox.xapi.executor.RateLimitedExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class XboxUserActivityService {

    private static final Logger log = LoggerFactory.getLogger(XboxUserActivityService.class);

    @Value("${ebot.limitPerUser:5}")
    private Integer limitPerUser;

    private final XApiClient xApiClient;

    private final XboxProfileRepository xboxProfileRepository;

    private final XboxTitleHistoryDataService xboxTitleHistoryDataService;

    private final RateLimitedExecutor rateLimitedExecutor;

    private final AchievementQueue achievementQueue;

    public XboxUserActivityService(XApiClient xApiClient,
                                   XboxProfileRepository xboxProfileRepository,
                                   XboxTitleHistoryDataService xboxTitleHistoryDataService,
                                   RateLimitedExecutor rateLimitedExecutor,
                                   AchievementQueue achievementQueue
    ) {
        this.xApiClient = xApiClient;
        this.xboxProfileRepository = xboxProfileRepository;
        this.xboxTitleHistoryDataService = xboxTitleHistoryDataService;
        this.rateLimitedExecutor = rateLimitedExecutor;
        this.achievementQueue = achievementQueue;
    }

    @Scheduled(fixedDelay = 5L, timeUnit = TimeUnit.MINUTES)
    public void allPlayersLatestAchievements() {
        Iterable<Profile> players = xboxProfileRepository.findAll();

        for (Profile player : players) {
            if (player.isActive()) {
                processPlayerAchievements(player);
            } else {
                log.info("Skipping {} - not active", player.getGamertag());
            }
        }
    }

    private void processPlayerAchievements(final Profile profile) {
        Objects.requireNonNull(profile);

        final Callable<TitleHubTitleList> titlesTask = () -> xApiClient.titleHubTitleList(profile.getId());
        final CompletableFuture<TitleHubTitleList> titlesFuture = rateLimitedExecutor.submit(titlesTask);


        try {
            final TitleHubTitleList titleHubTitles = titlesFuture.get();
            for (TitleHubTitle title : titleHubTitles.getTitles()) {
                if (xboxTitle(title)) {
                    TitleHistory titleHistory =
                            xboxTitleHistoryDataService.findTitleHistory(profile.getId(), title.getTitleId(), title.getName());

                    if (titleHistory == null) {
                        log.info("No title history for xuid={} and title={}, need to create", profile.getId(), title.getName());
                        final Callable<TitleHistory> titleHistoryTask = () -> xApiClient.titleHistory(profile.getId());
                        final CompletableFuture<TitleHistory> titleHistoryTaskResult = rateLimitedExecutor.submit(titleHistoryTask);
                        final TitleHistory xapiTitleHistory = titleHistoryTaskResult.get();
                        final Title xapiTitle = xapiTitleHistory.getTitles().stream()
                                .filter(t -> t.getTitleId().equals(title.getTitleId()))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("Title is null in TitleHistory response!"));
                        titleHistory = xboxTitleHistoryDataService.saveTitleHistory(profile, xapiTitle);
                    }

                    processAchievements(profile, titleHistory, title);
                }
            }
        } catch (Exception e) {
            log.error("Error communicating with TitleHub endpoint", e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void processAchievements(final Profile profile, final TitleHistory titleHistory, final TitleHubTitle title) {
        if (gamerscoreChanged(titleHistory, title)) {
            log.info("Gamerscore changed for xuid={} and title={}, need to update", profile.getId(), title.getName());

            Callable<TitleHubAchievements> achievementsTask =
                    () -> xApiClient.titleHubAchievements(profile.getId(), title.getTitleId());
            final CompletableFuture<TitleHubAchievements> achievementsTaskResult =
                    rateLimitedExecutor.submit(achievementsTask);

            achievementsTaskResult.thenAccept(response -> {
                final List<TitleHubAchievement> achievements = response.getAchievements();
                achievements.stream()
                        .filter(achievement -> newAchievement(achievement, profile))
                        .sorted(Comparator.reverseOrder())
                        .limit(limitPerUser)
                        .forEach(achievement -> achievementQueue.placeAchievement(profile, achievement));

                final TitleHubAchievement lastAchievement = achievements.stream()
                        .filter(achievement -> newAchievement(achievement, profile))
                        .max(Comparator.naturalOrder())
                        .orElseThrow(() -> new RuntimeException("Achievement is null. Weird."));

                xboxTitleHistoryDataService.updateTitleHistory(titleHistory, title, lastAchievement);
                updateUser(profile, lastAchievement);
            });
        }
    }

    private void updateUser(final Profile profile, final TitleHubAchievement achievement) {
        final Progression unlocked = achievement.getProgression();
        if (unlocked != null && unlocked.getTimeUnlocked().isAfter(profile.getLastAchievement())) {
            profile.setLastAchievement(unlocked.getTimeUnlocked());
            xboxProfileRepository.save(profile);
        } else {
            throw new IllegalArgumentException("Null progression for achievement=" + achievement.getName());
        }
    }

    private boolean xboxTitle(final TitleHubTitle title) {
        return title != null && title.getDevices() != null && !title.getDevices().contains("Win32");
    }

    private boolean newAchievement(final TitleHubAchievement achievement, final Profile player) {
        return achievement != null
                && achievement.getProgression() != null
                && player != null
                && achievement.getProgression().getTimeUnlocked().isAfter(player.getLastAchievement());
    }

    private boolean gamerscoreChanged(final TitleHistory stored, final TitleHubTitle got) {
        return stored.getCurrentGamescore() < got.getAchievement().getCurrentGamerscore();
    }

}
