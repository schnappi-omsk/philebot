package com.gundomrays.philebot.xbox.xapi;

import com.gundomrays.philebot.data.XboxProfileRepository;
import com.gundomrays.philebot.data.XboxTitleHistoryRepository;
import com.gundomrays.philebot.xbox.domain.Profile;
import com.gundomrays.philebot.xbox.domain.Title;
import com.gundomrays.philebot.xbox.domain.TitleHistory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class XboxGameStatsService {

    private final XboxProfileRepository xboxProfileRepository;

    private final XboxTitleHistoryRepository xboxTitleHistoryRepository;

    public XboxGameStatsService(XboxProfileRepository xboxProfileRepository,
                                XboxTitleHistoryRepository xboxTitleHistoryRepository) {
        this.xboxProfileRepository = xboxProfileRepository;
        this.xboxTitleHistoryRepository = xboxTitleHistoryRepository;
    }

    public Map<String, String> findGame(String input) {
        Iterable<Profile> gamers = xboxProfileRepository.findAll();

        Map<String, String> result = new HashMap<>();
        for (Profile gamer : gamers) {
            Iterable<TitleHistory> allByXuid = xboxTitleHistoryRepository.findAllByXuid(gamer.getId());
            for (TitleHistory history : allByXuid) {
                for (Title title : history.getTitles()) {
                    result.put(title.getTitleId(), title.getName());
                }
            }
        }
        return result;
    }

}
