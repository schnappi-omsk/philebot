package com.gundomrays.philebot.telegram.data;

import com.gundomrays.philebot.telegram.domain.ChatAwards;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ChatAwardsRepository extends CrudRepository<ChatAwards, String>, ChatAwardsRepositoryExtension {
    Optional<ChatAwards> findByAwardDate(LocalDate awardDate);
}
