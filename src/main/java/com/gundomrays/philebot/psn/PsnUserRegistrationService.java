package com.gundomrays.philebot.psn;

import com.gundomrays.philebot.data.PsnTrophyDataService;
import com.gundomrays.philebot.psn.api.PsnApiClient;
import com.gundomrays.philebot.psn.api.exception.PsnAccessDeniedException;
import com.gundomrays.philebot.psn.api.exception.PsnApiException;
import com.gundomrays.philebot.psn.auth.PsnAuthenticationException;
import com.gundomrays.philebot.psn.domain.PsnProfile;
import com.gundomrays.philebot.psn.domain.PsnTrophyTitle;
import com.gundomrays.philebot.psn.domain.PsnTrophyTitles;
import io.micrometer.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class PsnUserRegistrationService {

    private static final Logger log = LoggerFactory.getLogger(PsnUserRegistrationService.class);

    // Maximum page size of the trophy titles endpoint
    private static final int TITLES_PAGE_SIZE = 800;

    private final PsnApiClient psnApiClient;

    private final PsnTrophyDataService psnTrophyDataService;

    public PsnUserRegistrationService(PsnApiClient psnApiClient, PsnTrophyDataService psnTrophyDataService) {
        this.psnApiClient = psnApiClient;
        this.psnTrophyDataService = psnTrophyDataService;
    }

    public String registerUser(final String tgUserName, final Long tgId, final String onlineId) {
        Objects.requireNonNull(tgUserName);

        if (StringUtils.isBlank(onlineId)) {
            return "Format: /psnreg [PSN Online ID]";
        }

        final PsnProfile registered = psnTrophyDataService.profileByTgUsername(tgUserName);
        if (registered != null) {
            log.info("User with Telegram username {} already has PSN account {}", tgUserName, registered.getOnlineId());
            return String.format("User with Telegram username %s already has PSN account %s registered",
                    tgUserName, registered.getOnlineId());
        }

        log.info("PSN registration of Telegram user {} with Online ID {}", tgUserName, onlineId);
        try {
            final String accountId = lookupAccountId(onlineId);
            if (accountId == null) {
                return String.format("PSN account %s was not found", onlineId);
            }
            if (psnTrophyDataService.profileExists(accountId)) {
                return String.format("PSN account %s is already registered", onlineId);
            }

            final List<PsnTrophyTitle> titles;
            try {
                titles = allTrophyTitles(accountId);
            } catch (PsnAccessDeniedException e) {
                log.warn("Trophies of {} are not visible to the bot account: {}", onlineId, e.getMessage());
                return String.format("Trophies of %s are not visible to the bot account. "
                        + "Set trophy privacy to Anyone or add the bot account as a friend.", onlineId);
            }

            final PsnProfile profile = new PsnProfile();
            profile.setAccountId(accountId);
            profile.setOnlineId(onlineId);
            profile.setTgUsername(tgUserName);
            profile.setTgId(tgId);
            psnTrophyDataService.saveBaseline(profile, titles);

            return String.format("%s was successfully registered with PSN Online ID %s", tgUserName, onlineId);
        } catch (PsnAuthenticationException e) {
            log.error("PSN authentication failed during registration: {}", e.getMessage());
            return "PSN is not available now, try again later.";
        } catch (PsnApiException e) {
            log.error("Error registering PSN account for " + tgUserName, e);
            return "Error registering PSN account.";
        }
    }

    private String lookupAccountId(final String onlineId) {
        try {
            return psnApiClient.accountId(onlineId);
        } catch (PsnAccessDeniedException e) {
            log.warn("PSN account {} was not found: {}", onlineId, e.getMessage());
            return null;
        }
    }

    private List<PsnTrophyTitle> allTrophyTitles(final String accountId) {
        final List<PsnTrophyTitle> titles = new ArrayList<>();
        Integer offset = 0;
        while (offset != null) {
            final PsnTrophyTitles page = psnApiClient.trophyTitles(accountId, TITLES_PAGE_SIZE, offset);
            titles.addAll(page.getTrophyTitles());
            final Integer next = page.getNextOffset();
            final boolean hasMore = next != null && next > offset
                    && (page.getTotalItemCount() == null || next < page.getTotalItemCount());
            offset = hasMore ? next : null;
        }
        return titles;
    }

}
