package com.gundomrays.philebot.data;

import com.gundomrays.philebot.xbox.domain.TitleHistory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface XboxTitleHistoryRepository extends CrudRepository<TitleHistory, String> {
    Iterable<TitleHistory> findAllByXuid(String xuid);
}
