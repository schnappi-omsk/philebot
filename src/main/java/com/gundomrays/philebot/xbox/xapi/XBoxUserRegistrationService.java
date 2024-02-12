package com.gundomrays.philebot.xbox.xapi;

import com.gundomrays.philebot.data.XboxProfileRepository;
import com.gundomrays.philebot.data.XboxTitleHistoryDataService;
import com.gundomrays.philebot.xbox.domain.Profile;
import com.gundomrays.philebot.xbox.domain.Title;
import com.gundomrays.philebot.xbox.domain.TitleHistory;
import com.gundomrays.philebot.xbox.xapi.executor.RateLimitedExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.StreamSupport;

@Service
public class XBoxUserRegistrationService {

    private static final Logger log = LoggerFactory.getLogger(XBoxUserRegistrationService.class);

    private final XApiClient xApiClient;

    private final XboxProfileRepository xboxProfileRepository;

    private final XboxTitleHistoryDataService xboxTitleHistoryDataService;

    private final RateLimitedExecutor rateLimitedExecutor;


    public XBoxUserRegistrationService(XApiClient xApiClient,
                                       XboxProfileRepository xboxProfileRepository,
                                       XboxTitleHistoryDataService xboxTitleHistoryDataService,
                                       RateLimitedExecutor rateLimitedExecutor) {
        this.xApiClient = xApiClient;
        this.xboxProfileRepository = xboxProfileRepository;
        this.xboxTitleHistoryDataService = xboxTitleHistoryDataService;
        this.rateLimitedExecutor = rateLimitedExecutor;
    }

    public XboxServiceResponse registerUser(final String tgUserName, final Long tgId, final String gamerTag) {
        Objects.requireNonNull(tgUserName);
        Objects.requireNonNull(gamerTag);

        final Optional<Profile> profileHolder = xboxProfileRepository.findByTgUsername(tgUserName);

        if (profileHolder.isPresent()) {
            log.info("User with Telegram username {} is already registered", tgUserName);
            return new XboxServiceResponse(String.format("User with Telegram username %s is already registered", tgUserName));
        } else {
            log.info("Registration user with Telegram username {}", tgUserName);
            final Callable<XboxServiceResponse> registerTask = () -> {
                final Profile xboxProfile = xApiClient.userByGamertag(gamerTag);
                xboxProfile.setTgUsername(tgUserName);
                xboxProfile.setTgId(tgId);
                xboxProfileRepository.save(xboxProfile);

                fillProfileHistory(xboxProfile);
                return new XboxServiceResponse(String.format("%s was successfully registered with gamertag %s", tgUserName, gamerTag));
            };

            final CompletableFuture<XboxServiceResponse> future = rateLimitedExecutor.submit(2, registerTask);

            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                log.error("Error registering user with tgUserName = " + tgUserName, e);
                return new XboxServiceResponse("Error registering user.");
            }
        }
    }

    public void togglePing(final String tgUserName, final boolean ping) {
        final Profile profile = xboxProfileRepository.findByTgUsername(tgUserName).orElse(null);

        if (profile == null) {
            log.warn("No profile registered for Telegram user @{}", tgUserName);
            return;
        }

        if (profile.isPing() != ping) {
            profile.setPing(ping);
            log.info("New ping setting for @{}: {}", tgUserName, ping);
            xboxProfileRepository.save(profile);
        } else {
            log.info("Ping setting is already {} for user @{}", ping, tgUserName);
        }
    }

    public Profile retrieveUserProfileByTg(final String tgUsername) {
        return xboxProfileRepository.findByTgUsername(tgUsername).orElse(null);
    }

    public List<Profile> registeredUsers() {
        return StreamSupport.stream(xboxProfileRepository.findAll().spliterator(), false).toList();
    }

    public Profile retrieveUserProfile(final String xuid) {
        return xboxProfileRepository.findById(xuid).orElse(null);
    }

    private void fillProfileHistory(final Profile profile) {
        Objects.requireNonNull(profile);

        final TitleHistory titleHistory = xApiClient.titleHistory(profile.getId());
        List<Title> playedTitles = titleHistory.getTitles();
        log.info("User {} played {} titles", profile.getGamertag(), playedTitles.size());
        xboxTitleHistoryDataService.saveTitleHistory(profile, titleHistory);
        log.info("User {} title history was saved", profile.getGamertag());
    }

}
