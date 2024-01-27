package com.gundomrays.philebot.data;

import com.gundomrays.philebot.xbox.domain.Title;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface XboxTitleRepository extends CrudRepository<Title, String>, XboxTitleRepositoryExtension {
    Optional<Title> findByTitleId(String titleId);
}
