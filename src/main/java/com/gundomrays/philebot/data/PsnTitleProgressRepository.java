package com.gundomrays.philebot.data;

import com.gundomrays.philebot.psn.domain.PsnTitleProgress;
import com.gundomrays.philebot.psn.domain.PsnTitleProgressId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PsnTitleProgressRepository extends CrudRepository<PsnTitleProgress, PsnTitleProgressId> {
    List<PsnTitleProgress> findAllByAccountId(String accountId);
}
