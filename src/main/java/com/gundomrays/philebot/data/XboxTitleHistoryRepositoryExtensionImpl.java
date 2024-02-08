package com.gundomrays.philebot.data;

import com.gundomrays.philebot.xbox.domain.Gamerscore;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class XboxTitleHistoryRepositoryExtensionImpl implements XboxTitleHistoryRepositoryExtension {

    private static final String LEADERBOARD_SQL = "SELECT p.tg_username AS gamertag, sum(th.current_gamescore) AS score " +
            "FROM title_history th " +
            "    JOIN profile p ON th.xuid = p.xuid " +
            "WHERE th.total_gamescore != 0 AND th.current_gamescore !=0 " +
            "GROUP BY p.tg_username " +
            "ORDER BY score desc";

    @PersistenceContext
    private final EntityManager entityManager;

    public XboxTitleHistoryRepositoryExtensionImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<Gamerscore> leaderboard() {
        final Query leaderboardQuery = entityManager.createNativeQuery(LEADERBOARD_SQL);
        final List<Object[]> queryResult = leaderboardQuery.getResultList();
        List<Gamerscore> result = new ArrayList<>();

        for (Object[] row : queryResult) {
            Gamerscore gamerscore = new Gamerscore();
            gamerscore.setGamertag((String) row[0]);
            gamerscore.setScore((Long) row[1]);
            result.add(gamerscore);
        }
        return result;
    }

}
