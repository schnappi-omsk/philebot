package com.gundomrays.philebot.data;

import com.gundomrays.philebot.xbox.domain.Title;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
public class XboxTitleRepositoryExtensionImpl implements XboxTitleRepositoryExtension {

    private static final String FIND_TITLES = "SELECT t.title_id, t.name, t.title_img, t.platform " +
            "FROM title t " +
            "WHERE ";

    private static final String LAST_TITLE = "SELECT t.title_id, t.name, t.title_img, t.platform " +
            "FROM title t " +
            "JOIN title_history th ON t.title_id = th.title_id " +
            "ORDER BY th.last_updated DESC " +
            "LIMIT 1";


    @PersistenceContext
    private final EntityManager entityManager;

    public XboxTitleRepositoryExtensionImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Collection<Title> searchTitlesByName(String input) {
        String[] names = input.split(" ");
        final String queryText = FIND_TITLES + generateCondition(names) + orderBy();
        final Query searchQuery = entityManager.createNativeQuery(queryText, Title.class);

        for (int i = 0; i < names.length; i++) {
            searchQuery.setParameter("name" + i, "%" + names[i] + "%");
        }

        return searchQuery.getResultList();
    }

    @Override
    public Optional<Title> findLastPlayedTitle() {
        final Query lastTitleQuery = entityManager.createNativeQuery(LAST_TITLE, Title.class);
        return Optional.ofNullable((Title) lastTitleQuery.getSingleResult());
    }

    private String generateCondition(final String[] names) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < names.length; i++) {
            builder.append("t.name ILIKE :name").append(i);
            if (i < names.length - 1) {
                builder.append(" AND ");
            }
        }
        return builder.toString();
    }

    private String orderBy() {
        return " ORDER BY t.name";
    }
}
