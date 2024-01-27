package com.gundomrays.philebot.xbox.xapi;

import com.gundomrays.philebot.data.XboxProfileRepository;
import com.gundomrays.philebot.data.XboxTitleHistoryDataService;
import com.gundomrays.philebot.xbox.domain.Profile;
import com.gundomrays.philebot.xbox.domain.TitleHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class XBoxUserRegistrationService {

    private static final Logger log = LoggerFactory.getLogger(XBoxUserRegistrationService.class);

    private final XApiClient xApiClient;

    private final XboxProfileRepository xboxProfileRepository;

    private final XboxTitleHistoryDataService xboxTitleHistoryDataService;


    public XBoxUserRegistrationService(XApiClient xApiClient,
                                       XboxProfileRepository xboxProfileRepository,
                                       XboxTitleHistoryDataService xboxTitleHistoryDataService) {
        this.xApiClient = xApiClient;
        this.xboxProfileRepository = xboxProfileRepository;
        this.xboxTitleHistoryDataService = xboxTitleHistoryDataService;
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
            final Profile xboxProfile = xApiClient.userByGamertag(gamerTag);
            xboxProfile.setTgUsername(tgUserName);
            xboxProfile.setTgId(tgId);
            xboxProfileRepository.save(xboxProfile);

            fillProfileHistory(xboxProfile);
            return new XboxServiceResponse(String.format("%s was successfully registered with gamertag %s", tgUserName, gamerTag));
        }
    }

    public Profile retrieveUserProfile(final String xuid) {
        return xboxProfileRepository.findById(xuid).orElse(null);
    }

    public boolean checkUserRegistration(final String tgUsername) {
        Objects.requireNonNull(tgUsername);

        return xboxProfileRepository.findByTgUsername(tgUsername).isPresent();
    }

    private void fillProfileHistory(final Profile profile) {
        Objects.requireNonNull(profile);

        final TitleHistory titleHistory = xApiClient.titleHistory(profile.getId());
        log.info("User {} played {} titles", profile.getGamertag(), titleHistory.getTitles().size());
        xboxTitleHistoryDataService.saveTitleHistory(titleHistory);
        log.info("User {} title history was saved", profile.getGamertag());
    }

}
