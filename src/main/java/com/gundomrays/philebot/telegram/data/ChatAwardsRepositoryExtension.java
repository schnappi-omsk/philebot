package com.gundomrays.philebot.telegram.data;

import com.gundomrays.philebot.telegram.domain.AwardScore;

import java.util.List;

public interface ChatAwardsRepositoryExtension {
    List<AwardScore> weeklyLeaderboard();
    List<AwardScore> monthlyLeaderboard();
    List<AwardScore> overallLeaderboard();
}
