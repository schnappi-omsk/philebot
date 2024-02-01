package com.gundomrays.philebot.data;

import com.gundomrays.philebot.xbox.domain.Image;
import com.gundomrays.philebot.xbox.domain.Title;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class XboxTitleDataService {

    private final XboxTitleRepository xboxTitleRepository;

    public XboxTitleDataService(XboxTitleRepository xboxTitleRepository) {
        this.xboxTitleRepository = xboxTitleRepository;
    }

    public Collection<Title> searchTitles(final String input) {
        return xboxTitleRepository.searchTitlesByName(input);
    }

    public Title lastPlayedTitle() {
        return xboxTitleRepository.findLastPlayedTitle().orElse(null);
    }

    public Title titleById(final String id) {
        return xboxTitleRepository.findByTitleId(id).orElse(null);
    }

    public Title saveTitle(final String titleId, final String titleName) {
        return saveTitle(titleId, titleName, null);
    }

    public Title saveTitle(final String titleId, final String titleName, final Image image) {
        final Title title = new Title();
        title.setTitleId(titleId);
        title.setName(titleName);
        if (image != null) {
            title.setTitleImg(image.getUrl());
        }
        return saveTitle(title);
    }

    public Title saveTitle(final Title title) {
        Optional<Title> stored = xboxTitleRepository.findByTitleId(title.getTitleId());

        Image art = extractImage(title, "TitledHeroArt");

        if (art == null) {
            art = extractImage(title, "SuperHeroArt");
        }

        if (art != null && title.getTitleImg() == null) {
            title.setTitleImg(art.getUrl());
        }
        if (stored.isPresent()) {
            Title storedTitle = stored.get();
            if (title.getTitleImg() != null) {
                storedTitle.setTitleImg(title.getTitleImg());
            }
            if (title.getPlatform() == null || title.getPlatform().isEmpty()) {
                storedTitle.setPlatform(gamePlatform(storedTitle));
            }
            return xboxTitleRepository.save(storedTitle);
        } else {
            title.setPlatform(gamePlatform(title));
            return stored.orElse(xboxTitleRepository.save(title));
        }
    }

    private String gamePlatform(final Title title) {
        if (title.getDevices() != null) {
            return String.join(", ", title.getDevices());
        }

        return title.getPlatform();
    }

    private Image extractImage(final Title title, final String type) {
        return title.getImages()
                .stream()
                .filter(img -> img.getType().equalsIgnoreCase(type))
                .findFirst()
                .orElse(null);
    }
}
