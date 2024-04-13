package com.gundomrays.philebot.telegram.data;

import com.gundomrays.philebot.telegram.domain.AwardMsg;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AwardMsgRepository extends CrudRepository<AwardMsg, Integer> {
}
