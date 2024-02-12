package com.gundomrays.philebot.xbox.xapi;

import com.gundomrays.philebot.data.XboxTitleDataService;
import com.gundomrays.philebot.xbox.domain.Title;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class XboxTitleService {

    private static final String PC = "PC";
    private static final String XBOX = "XBox";

    private final XboxTitleDataService xboxTitleDataService;

    public XboxTitleService(XboxTitleDataService xboxTitleDataService) {
        this.xboxTitleDataService = xboxTitleDataService;
    }

    public Title findTitle(final String titleId) {
        return xboxTitleDataService.titleById(titleId);
    }

    public Map<String, String> searchGames(final String userInput) {
        final Collection<Title> titles = xboxTitleDataService.searchTitles(userInput);

        final Map<String, String> result = new HashMap<>();
        for (Title title : titles) {
            final String platforms = platforms(title.getPlatform());
            final String gameLine = platforms.isEmpty()
                    ? title.getName()
                    : String.format("%s <strong>(%s)</strong>", title.getName(), platforms);
            result.put(gameLine, String.format("/game%s", title.getTitleId()));
        }

        return result;
    }

    private String platforms(final String input) {
        String inputLower = input.toLowerCase();
        if (inputLower.contains("pc") && inputLower.contains("xbox")) {
            return "";
        }
        return inputLower.contains("pc") ? PC : XBOX;
    }

}
