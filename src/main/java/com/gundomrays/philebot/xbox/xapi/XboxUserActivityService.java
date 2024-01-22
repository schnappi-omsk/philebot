package com.gundomrays.philebot.xbox.xapi;

import com.gundomrays.philebot.data.XboxProfileRepository;
import com.gundomrays.philebot.xbox.domain.Activity;
import com.gundomrays.philebot.xbox.domain.ActivityItem;
import com.gundomrays.philebot.xbox.domain.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class XboxUserActivityService {

    private static final Logger log = LoggerFactory.getLogger(XboxUserActivityService.class);

    @Value("${ebot.limitPerUser:5}")
    private Integer limitPerUser;

    private final XApiClient xApiClient;

    private final XboxProfileRepository xboxProfileRepository;

    public XboxUserActivityService(XApiClient xApiClient,
                                   XboxProfileRepository xboxProfileRepository
    ) {
        this.xApiClient = xApiClient;
        this.xboxProfileRepository = xboxProfileRepository;
    }

    public List<ActivityItem> allPlayersLatestAchievements() {
        final List<ActivityItem> activities = new ArrayList<>();
        Iterable<Profile> players = xboxProfileRepository.findAll();

        for (Profile player : players) {
            log.info("Retrieving activity of: {}", player.getGamertag());
            final LocalDateTime lastAchievement = player.getLastAchievement();
            Activity playerActivity = playerActivity(player);
            playerActivity.getActivityItems().stream()
                    .filter(item -> item.getDate().isAfter(lastAchievement))
                    .sorted(Comparator.reverseOrder())
                    .limit(limitPerUser)
                    .forEach(activities::add);
        }

        return activities;
    }

    public Activity playerActivity(final Profile profile) {
        Objects.requireNonNull(profile);

        return playerActivity(profile.getGamertag());
    }

    public Activity playerActivity(final String gamertag) {
        final Profile xboxProfile = xboxProfileRepository.findByGamertag(gamertag)
                .orElseThrow(() -> new RuntimeException("User is not registered in the app: " + gamertag));

        final Activity playerActivity = xApiClient.userActivity(xboxProfile.getId());
        playerActivity.getActivityItems().stream()
                .max(Comparator.naturalOrder())
                .filter(item -> item.getDate().isAfter(xboxProfile.getLastAchievement()))
                .ifPresent(item -> {
                    xboxProfile.setLastAchievement(item.getDate());
                    xboxProfileRepository.save(xboxProfile);
                    log.info("User's {} last activity was updated", gamertag);
                });

        return playerActivity;
    }

}
