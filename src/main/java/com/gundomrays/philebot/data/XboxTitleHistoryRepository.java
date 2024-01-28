package com.gundomrays.philebot.data;

import com.gundomrays.philebot.xbox.domain.Title;
import com.gundomrays.philebot.xbox.domain.TitleHistory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface XboxTitleHistoryRepository extends CrudRepository<TitleHistory, String> {
    Iterable<TitleHistory> findAllByXuid(String xuid);
    Iterable<TitleHistory> findAllByTitleOrderByCurrentGamescoreDesc(Title title);
    Optional<TitleHistory> findByXuidAndTitle(String xuid, Title title);
}
