package com.gundomrays.philebot.data;

import com.gundomrays.philebot.xbox.domain.Title;

import java.util.Collection;

public interface XboxTitleRepositoryExtension {
    Collection<Title> searchTitlesByName(String name);
}
