package com.gundomrays.philebot.xbox.xapi;

import com.gundomrays.philebot.data.XboxTitleDataService;
import com.gundomrays.philebot.xbox.domain.Title;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service
public class XboxTitleService {

    private final XboxTitleDataService xboxTitleDataService;

    public XboxTitleService(XboxTitleDataService xboxTitleDataService) {
        this.xboxTitleDataService = xboxTitleDataService;
    }

    public Title findTitle(final String titleId) {
        return xboxTitleDataService.titleById(titleId);
    }

    public Map<String, String> searchGames(final String userInput) {
        return xboxTitleDataService.searchTitles(userInput);
    }

}
