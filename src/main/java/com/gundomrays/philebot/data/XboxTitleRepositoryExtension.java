package com.gundomrays.philebot.data;

import com.gundomrays.philebot.xbox.domain.Title;

import java.util.Collection;
import java.util.Optional;

public interface XboxTitleRepositoryExtension {
    Collection<Title> searchTitlesByName(String name);

    Optional<Title> findLastPlayedTitle();
}
