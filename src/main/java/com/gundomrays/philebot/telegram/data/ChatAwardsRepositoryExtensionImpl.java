package com.gundomrays.philebot.telegram.data;

import com.gundomrays.philebot.telegram.domain.AwardScore;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChatAwardsRepositoryExtensionImpl implements ChatAwardsRepositoryExtension {

    public static final String LEADERBOARD_SQL = "SELECT nom.tg_name, COUNT(aw.award_date) AS award_count " +
            "FROM chat_awards aw " +
            "JOIN award_nominee nom ON aw.tg_id = nom.tg_id " +
            "WHERE aw.award_date >= :startDate " +
            "GROUP BY nom.tg_name " +
            "ORDER BY award_count DESC";

    @PersistenceContext
    private final EntityManager entityManager;

    public ChatAwardsRepositoryExtensionImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<AwardScore> weeklyLeaderboard() {
        final LocalDate thisMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return leaderboardFrom(thisMonday);
    }

    @Override
    public List<AwardScore> monthlyLeaderboard() {
        final LocalDate firstOfThisMonth = LocalDate.now().withDayOfMonth(1);
        return leaderboardFrom(firstOfThisMonth);
    }

    @Override
    public List<AwardScore> overallLeaderboard() {
        return leaderboardFrom(LocalDate.EPOCH);
    }

    private List<AwardScore> leaderboardFrom(final LocalDate startDate) {
        Query query = entityManager.createNativeQuery(LEADERBOARD_SQL);
        query.setParameter("startDate", startDate);
        List<Object[]> queryResult = query.getResultList();
        List<AwardScore> result = new ArrayList<>();

        for (Object[] row : queryResult) {
            AwardScore awardScore = new AwardScore();
            awardScore.setNomineeName((String) row[0]);
            awardScore.setAwardCount((Long) row[1]);
            result.add(awardScore);
        }

        return result;
    }
}
