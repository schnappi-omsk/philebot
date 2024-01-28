package com.gundomrays.philebot.data;

import com.gundomrays.philebot.xbox.domain.Title;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
public class XboxTitleDataService {

    private final XboxTitleRepository xboxTitleRepository;

    public XboxTitleDataService(XboxTitleRepository xboxTitleRepository) {
        this.xboxTitleRepository = xboxTitleRepository;
    }

    public Map<String, String> searchTitles(final String input) {
        final Collection<Title> titles = xboxTitleRepository.searchTitlesByName(input);
        final Map<String, String> result = new HashMap<>();
        for (Title title : titles) {
            result.put(title.getName(), String.format("/game%s", title.getTitleId()));
        }
        return result;
    }

    public Title titleById(final String id) {
        return xboxTitleRepository.findByTitleId(id).orElse(null);
    }

}
