package com.gundomrays.philebot.web.xbox;

import com.gundomrays.philebot.data.XboxTitleDataService;
import com.gundomrays.philebot.data.XboxTitleHistoryDataService;
import com.gundomrays.philebot.xbox.domain.Title;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Service
public class XboxTitleUIService {

    private final XboxTitleDataService xboxTitleDataService;

    private final XboxTitleHistoryDataService xboxTitleHistoryDataService;

    public XboxTitleUIService(XboxTitleDataService xboxTitleDataService, XboxTitleHistoryDataService xboxTitleHistoryDataService) {
        this.xboxTitleDataService = xboxTitleDataService;
        this.xboxTitleHistoryDataService = xboxTitleHistoryDataService;
    }

    public Collection<String> alphabet() {
        return xboxTitleDataService.titleAlphabet();
    }

    public Title title(final String titleId) {
        return xboxTitleDataService.titleById(titleId);
    }

    public Map<String, Integer> stats(final Title title) {
        if (title == null) {
            return Collections.emptyMap();
        }

        return xboxTitleHistoryDataService.getTitleStatistics(title);
    }

}
