package com.gundomrays.philebot.xbox.xapi;

import com.gundomrays.philebot.data.XboxTitleDataService;
import com.gundomrays.philebot.xbox.domain.Title;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class XboxTitleService {

    private static final String PC = "PC";
    private static final String XBOX_SERIES = "XBoxSeries";
    private static final String XONE = "XOne";
    private static final String X360 = "X360";

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
            result.put(
                    String.format("%s <strong>(%s)</strong>", title.getName(), platforms(title.getPlatform())),
                    String.format("/game%s", title.getTitleId())
            );
        }

        return result;
    }

    private String platforms(final String input) {
        if (!input.contains(",")) {
            return input;
        }

        String inputLower = input.toLowerCase();

        if (checkPlatform(inputLower, "pc")) {
            return PC + "|" + selectPlatform(inputLower);
        } else {
            return selectPlatform(inputLower);
        }
    }

    private boolean checkPlatform(final String input, final String substring) {
        return input.contains(substring);
    }

    private String selectPlatform(final String input) {
        if (checkPlatform(input, "series")) {
            return XBOX_SERIES;
        } else if (checkPlatform(input, "one")) {
            return XONE;
        } else {
            return X360;
        }
    }
}
