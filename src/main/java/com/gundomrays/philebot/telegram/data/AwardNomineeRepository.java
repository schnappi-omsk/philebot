package com.gundomrays.philebot.telegram.data;

import com.gundomrays.philebot.telegram.domain.AwardNominee;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AwardNomineeRepository extends CrudRepository<AwardNominee, String> {
}
