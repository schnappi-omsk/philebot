package com.gundomrays.philebot.data;

import com.gundomrays.philebot.xbox.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
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

    public TitleHistory findTitleHistory(final String xuid, final ActivityItem item) {
        Title title = saveTitle(item.getTitleId(), item.getContentTitle());
        return xboxTitleHistoryRepository.findByXuidAndTitle(xuid, title).orElse(null);
    }

    public void saveTitleHistory(final Profile profile, final TitleHistory titleHistory) {
        titleHistory.getTitles().forEach(title -> saveTitleHistory(profile, title));
    }

    public TitleHistory saveTitleHistory(final Profile profile, final Title title) {
        Image art = extractImage(title, "TitledHeroArt");

        if (art == null) {
            art = extractImage(title, "SuperHeroArt");
        }

        TitleHistory toStore = xboxTitleHistoryRepository
                .findByXuidAndTitle(profile.getId(), title).orElse(null);
        if (toStore == null) {
            toStore = new TitleHistory();
            toStore.setXuid(profile.getId());
            toStore.setTitle(saveTitle(title.getTitleId(), title.getName(), art));
        }

        toStore.setCurrentGamescore(title.getAchievement().getCurrentGamerscore());
        toStore.setTotalGamescore(title.getAchievement().getTotalGamerscore());
        toStore.setLastUpdated(profile.getLastAchievement());
        TitleHistory stored = xboxTitleHistoryRepository.save(toStore);
        log.info("Stored title history for xuid={} and game={}, art={}",
                stored.getXuid(), stored.getTitle().getName(), art);
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

    private Title saveTitle(final Title title) {
        Optional<Title> stored = xboxTitleRepository.findByTitleId(title.getTitleId());
        if (stored.isPresent()) {
            Title storedTitle = stored.get();
            storedTitle.setTitleImg(title.getTitleImg());
            return xboxTitleRepository.save(storedTitle);
        } else {
            return stored.orElse(xboxTitleRepository.save(title));
        }
    }

    private Title saveTitle(final String titleId, final String titleName) {
        return saveTitle(titleId, titleName, null);
    }

    private Title saveTitle(final String titleId, final String titleName, final Image image) {
        final Title title = new Title();
        title.setTitleId(titleId);
        title.setName(titleName);
        if (image != null) {
            title.setTitleImg(image.getUrl());
        }
        return saveTitle(title);
    }

    private Image extractImage(Title title, String type) {
        return title.getImages()
                .stream()
                .filter(img -> img.getType().equalsIgnoreCase(type))
                .findFirst()
                .orElse(null);
    }

}
