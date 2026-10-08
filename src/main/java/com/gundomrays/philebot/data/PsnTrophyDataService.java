package com.gundomrays.philebot.data;

import com.gundomrays.philebot.psn.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class PsnTrophyDataService {

    private static final Logger log = LoggerFactory.getLogger(PsnTrophyDataService.class);

    private final PsnProfileRepository psnProfileRepository;

    private final PsnTitleProgressRepository psnTitleProgressRepository;

    private final PsnEarnedTrophyRepository psnEarnedTrophyRepository;

    public PsnTrophyDataService(PsnProfileRepository psnProfileRepository,
                                PsnTitleProgressRepository psnTitleProgressRepository,
                                PsnEarnedTrophyRepository psnEarnedTrophyRepository) {
        this.psnProfileRepository = psnProfileRepository;
        this.psnTitleProgressRepository = psnTitleProgressRepository;
        this.psnEarnedTrophyRepository = psnEarnedTrophyRepository;
    }

    public List<PsnProfile> profiles() {
        return StreamSupport.stream(psnProfileRepository.findAll().spliterator(), false).toList();
    }

    public PsnProfile profileByTgUsername(final String tgUsername) {
        return psnProfileRepository.findByTgUsername(tgUsername).orElse(null);
    }

    public boolean profileExists(final String accountId) {
        return psnProfileRepository.existsById(accountId);
    }

    public void saveProfile(final PsnProfile profile) {
        psnProfileRepository.save(profile);
    }

    public Map<String, PsnTitleProgress> titleProgress(final String accountId) {
        return psnTitleProgressRepository.findAllByAccountId(accountId)
                .stream()
                .collect(Collectors.toMap(PsnTitleProgress::getNpCommunicationId, Function.identity()));
    }

    public Set<Integer> earnedTrophyIds(final String accountId, final String npCommunicationId) {
        return psnEarnedTrophyRepository.findAllByAccountIdAndNpCommunicationId(accountId, npCommunicationId)
                .stream()
                .map(PsnEarnedTrophy::getTrophyId)
                .collect(Collectors.toSet());
    }

    // Stores the profile and the current state of its titles without tracking single trophies,
    // so trophies earned before the registration are never announced
    @Transactional
    public void saveBaseline(final PsnProfile profile, final List<PsnTrophyTitle> titles) {
        psnProfileRepository.save(profile);
        titles.forEach(title -> psnTitleProgressRepository.save(
                toTitleProgress(profile.getAccountId(), title, null, false)));
        log.info("Stored PSN baseline for {}: {} titles", profile.getOnlineId(), titles.size());
    }

    @Transactional
    public void saveTitleProgress(final PsnProfile profile, final PsnTrophyTitle title,
                                  final PsnTitleProgress stored, final List<PsnUserTrophy> earned) {
        final String accountId = profile.getAccountId();
        final Set<Integer> known = earnedTrophyIds(accountId, title.getNpCommunicationId());
        earned.stream()
                .filter(trophy -> !known.contains(trophy.getTrophyId()))
                .forEach(trophy -> psnEarnedTrophyRepository.save(toEarnedTrophy(accountId, title, trophy)));
        psnTitleProgressRepository.save(toTitleProgress(accountId, title, stored, true));
        log.info("PSN title progress updated for {} and title={}", profile.getOnlineId(), title.getTrophyTitleName());
    }

    private PsnTitleProgress toTitleProgress(final String accountId, final PsnTrophyTitle title,
                                             final PsnTitleProgress stored, final boolean trophiesTracked) {
        final PsnTitleProgress progress = stored != null ? stored : new PsnTitleProgress();
        progress.setAccountId(accountId);
        progress.setNpCommunicationId(title.getNpCommunicationId());
        progress.setNpServiceName(title.getNpServiceName());
        progress.setTitleName(title.getTrophyTitleName());
        progress.setTitleIconUrl(title.getTrophyTitleIconUrl());
        progress.setPlatform(title.getTrophyTitlePlatform());
        final PsnTrophyCounts earned = title.getEarnedTrophies();
        if (earned != null) {
            progress.setEarnedBronze(earned.getBronze());
            progress.setEarnedSilver(earned.getSilver());
            progress.setEarnedGold(earned.getGold());
            progress.setEarnedPlatinum(earned.getPlatinum());
        }
        progress.setProgress(title.getProgress());
        progress.setLastUpdated(title.getLastUpdatedDateTime());
        progress.setTrophiesTracked(trophiesTracked);
        return progress;
    }

    private PsnEarnedTrophy toEarnedTrophy(final String accountId, final PsnTrophyTitle title, final PsnUserTrophy trophy) {
        final PsnEarnedTrophy earnedTrophy = new PsnEarnedTrophy();
        earnedTrophy.setAccountId(accountId);
        earnedTrophy.setNpCommunicationId(title.getNpCommunicationId());
        earnedTrophy.setTrophyId(trophy.getTrophyId());
        earnedTrophy.setTrophyType(trophy.getTrophyType());
        earnedTrophy.setEarnedAt(trophy.getEarnedDateTime());
        return earnedTrophy;
    }

}
