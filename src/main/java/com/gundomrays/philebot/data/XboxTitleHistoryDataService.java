package com.gundomrays.philebot.data;

import com.gundomrays.philebot.xbox.domain.Profile;
import com.gundomrays.philebot.xbox.domain.Title;
import com.gundomrays.philebot.xbox.domain.TitleHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Service
public class XboxTitleHistoryDataService {

    private static final Logger log = LoggerFactory.getLogger(XboxTitleHistoryDataService.class);

    private final XboxTitleHistoryRepository xboxTitleHistoryRepository;

    private final XboxTitleRepository xboxTitleRepository;

    private final XboxProfileRepository xboxProfileRepository;

    public XboxTitleHistoryDataService(XboxTitleHistoryRepository xboxTitleHistoryRepository,
                                       XboxTitleRepository xboxTitleRepository,
                                       XboxProfileRepository xboxProfileRepository) {
        this.xboxTitleHistoryRepository = xboxTitleHistoryRepository;
        this.xboxTitleRepository = xboxTitleRepository;
        this.xboxProfileRepository = xboxProfileRepository;
    }

    public void saveTitleHistory(final TitleHistory titleHistory) {
        titleHistory.getTitles().forEach(title -> {
            TitleHistory toStore = new TitleHistory();
            toStore.setXuid(titleHistory.getXuid());
            toStore.setTitle(saveTitle(title));
            toStore.setCurrentGamescore(title.getAchievement().getCurrentGamerscore());
            toStore.setTotalGamescore(title.getAchievement().getTotalGamerscore());
            TitleHistory stored = xboxTitleHistoryRepository.save(toStore);
            log.info("Stored title history for xuid={} and game={}", stored.getXuid(), stored.getTitle().getName());
        });
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
            final Integer completion = (int) ((double) history.getCurrentGamescore() / history.getTotalGamescore() * 100);
            result.put(completion, gamer.getGamertag());
        }
        return result;
    }

    private Title saveTitle(final Title title) {
        return xboxTitleRepository.findByTitleId(title.getTitleId())
                .orElse(xboxTitleRepository.save(title));
    }

}
