package com.gundomrays.philebot.data;

import com.gundomrays.philebot.psn.domain.PsnEarnedTrophy;
import com.gundomrays.philebot.psn.domain.PsnEarnedTrophyId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PsnEarnedTrophyRepository extends CrudRepository<PsnEarnedTrophy, PsnEarnedTrophyId> {
    List<PsnEarnedTrophy> findAllByAccountIdAndNpCommunicationId(String accountId, String npCommunicationId);
}
