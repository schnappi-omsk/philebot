package com.gundomrays.philebot.data;

import com.gundomrays.philebot.xbox.domain.Gamerscore;

import java.util.List;

public interface XboxTitleHistoryRepositoryExtension {
    List<Gamerscore> leaderboard();
}
