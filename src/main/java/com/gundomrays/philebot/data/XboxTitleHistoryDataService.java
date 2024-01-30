package com.gundomrays.philebot.data;

import com.gundomrays.philebot.xbox.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

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

    public TitleHistory findTitleHistory(final String xuid, final ActivityItem item) {
        Title title = xboxTitleDataService.saveTitle(item.getTitleId(), item.getContentTitle());
        return xboxTitleHistoryRepository.findByXuidAndTitle(xuid, title).orElse(null);
    }

    public void saveTitleHistory(final Profile profile, final TitleHistory titleHistory) {
        titleHistory.getTitles().forEach(title -> {
            if (!title.getDevices().contains("Win32")) {
                saveTitleHistory(profile, title);
            }
        });
    }

    public TitleHistory saveTitleHistory(final Profile profile, final Title title) {
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

    public void updateTitleHistory(final TitleHistory titleHistory, final ActivityItem item) {
        Integer gamerscore = titleHistory.getCurrentGamescore() + item.getGamerscore();
        titleHistory.setCurrentGamescore(gamerscore);
        log.info("Gamerscore for xuid={} and title={} was updated by {}, new value={}",
                titleHistory.getXuid(), titleHistory.getTitle().getName(), item.getGamerscore(), gamerscore);

        if (titleHistory.getLastUpdated().isBefore(item.getDate())) {
            titleHistory.setLastUpdated(item.getDate());
            log.info("LastUpdated={} TitleHistory for xuid={} and title={}",
                    item.getDate(), titleHistory.getXuid(), titleHistory.getTitle().getTitleId());
        }
        xboxTitleHistoryRepository.save(titleHistory);
    }

    public Map<Integer, String> getTitleStatistics(final Title title) {
        Iterable<TitleHistory> gameStats = xboxTitleHistoryRepository.findAllByTitleOrderByCurrentGamescoreDesc(title);
        final Map<Integer, String> result = new TreeMap<>(Comparator.reverseOrder());
        for (TitleHistory history : gameStats) {
            final Profile gamer = xboxProfileRepository.findById(history.getXuid()).orElse(null);
            if (gamer == null) {
                log.warn("Gamer for titleHistory with ID={} is null (provided XUID={})",
                        history.getTitleHistoryId(), history.getXuid());
                continue;
            }
            final Integer completion = (int) Math.round((double) history.getCurrentGamescore() / history.getTotalGamescore() * 100);
            result.put(completion, gamer.getGamertag());
        }
        return result;
    }

}
