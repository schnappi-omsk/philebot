package com.gundomrays.philebot.data;

import com.gundomrays.philebot.xbox.domain.Title;
import com.gundomrays.philebot.xbox.domain.TitleHistory;
import org.springframework.stereotype.Service;

@Service
public class XboxTitleHistoryDataService {

    private final XboxTitleHistoryRepository xboxTitleHistoryRepository;

    private final XboxTitleRepository xboxTitleRepository;

    public XboxTitleHistoryDataService(XboxTitleHistoryRepository xboxTitleHistoryRepository,
                                       XboxTitleRepository xboxTitleRepository) {
        this.xboxTitleHistoryRepository = xboxTitleHistoryRepository;
        this.xboxTitleRepository = xboxTitleRepository;
    }

    public TitleHistory saveTitleHistory(final TitleHistory titleHistory) {
        titleHistory.getTitles().forEach(this::saveTitle);

        return xboxTitleHistoryRepository.save(titleHistory);
    }

    private Title saveTitle(final Title title) {
        return xboxTitleRepository.findByTitleId(title.getTitleId())
                .orElse(xboxTitleRepository.save(title));
    }

}
