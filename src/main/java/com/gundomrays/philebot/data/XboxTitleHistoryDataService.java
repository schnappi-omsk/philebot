package com.gundomrays.philebot.data;

import com.gundomrays.philebot.xbox.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class XboxTitleHistoryDataService {

    private static final Logger log = LoggerFactory.getLogger(XboxTitleHistoryDataService.class);

    private final XboxTitleHistoryRepository xboxTitleHistoryRepository;

    private final XboxProfileRepository xboxProfileRepository;

    private final XboxTitleDataService xboxTitleDataService;

    public XboxTitleHistoryDataService(XboxTitleHistoryRepository xboxTitleHistoryRepository,
                                       XboxProfileRepository xboxProfileRepository, XboxTitleDataService xboxTitleDataService) {
        this.xboxTitleHistoryRepository = xboxTitleHistoryRepository;
        this.xboxProfileRepository = xboxProfileRepository;
        this.xboxTitleDataService = xboxTitleDataService;
    }

    public TitleHistory findTitleHistory(final String xuid, final String titleId, final String titleName) {
        Title title = xboxTitleDataService.saveTitle(titleId, titleName);
        return xboxTitleHistoryRepository.findByXuidAndTitle(xuid, title).orElse(null);
    }

    @Transactional
    public void saveTitleHistory(final Profile profile, final TitleHistory titleHistory) {
        titleHistory.getTitles().forEach(title -> {
            if (!title.getDevices().contains("Win32")) {
                storeTitleHistory(profile, title);
            }
        });
    }

    @Transactional
    public TitleHistory saveTitleHistory(final Profile profile, final Title title) {
        return storeTitleHistory(profile, title);
    }

    @Transactional
    public void updateTitleHistory(final TitleHistory titleHistory, final TitleHubTitle title, final TitleHubAchievement achievement) {
        final Achievement progress = title.getAchievement();
        if (progress != null) {
            titleHistory.setCurrentGamescore(progress.getCurrentGamerscore());
            log.info("Current gamescore was updated to {}, xuid={}, game={}", titleHistory.getCurrentGamescore(), titleHistory.getXuid(), title.getName());
            if (!Objects.equals(titleHistory.getTotalGamescore(), progress.getTotalGamerscore())) {
                log.info("Total gamerscore was changed for {} (DLC released?)", titleHistory.getTitle().getName());
                titleHistory.setTotalGamescore(progress.getTotalGamerscore());
            }
        }

        final Progression unlocked = achievement.getProgression();
        boolean lastUpdatedChanged = unlocked != null && titleHistory.getLastUpdated().isBefore(unlocked.getTimeUnlocked());
        if (lastUpdatedChanged) {
            titleHistory.setLastUpdated(unlocked.getTimeUnlocked());
            log.info("LastUpdated={}. xuid={}, game={}", titleHistory.getLastUpdated(), titleHistory.getXuid(), title.getName());
        }
        xboxTitleHistoryRepository.save(titleHistory);
    }

    public Map<String, Integer> getTitleStatistics(final Title title) {
        Iterable<TitleHistory> gameStats = xboxTitleHistoryRepository.findAllByTitleOrderByCurrentGamescoreDesc(title);
        final Map<String, Integer> result = new TreeMap<>(Comparator.reverseOrder());
        for (TitleHistory history : gameStats) {
            final Profile gamer = xboxProfileRepository.findById(history.getXuid()).orElse(null);
            if (gamer == null) {
                log.warn("Gamer for titleHistory with ID={} is null (provided XUID={})",
                        history.getTitleHistoryId(), history.getXuid());
                continue;
            }
            final Integer completion = (int) Math.floor((double) history.getCurrentGamescore() / history.getTotalGamescore() * 100);
            result.put(gamer.getTgUsername(), completion);
        }
        return result.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }

    private TitleHistory storeTitleHistory(final Profile profile, final Title title) {
        TitleHistory toStore = xboxTitleHistoryRepository
                .findByXuidAndTitle(profile.getId(), title).orElse(null);
        if (toStore == null) {
            toStore = new TitleHistory();
            toStore.setXuid(profile.getId());
            toStore.setTitle(xboxTitleDataService.saveTitle(title));
        }

        toStore.setCurrentGamescore(title.getAchievement().getCurrentGamerscore());
        toStore.setTotalGamescore(title.getAchievement().getTotalGamerscore());
        toStore.setLastUpdated(profile.getLastAchievement());
        TitleHistory stored = xboxTitleHistoryRepository.save(toStore);
        log.info("Stored title history for xuid={} and game={}, art={}",
                stored.getXuid(), stored.getTitle().getName(), stored.getTitle().getTitleImg());
        return stored;
    }

    public List<Gamerscore> leaderboard() {
        return xboxTitleHistoryRepository.leaderboard();
    }

}
