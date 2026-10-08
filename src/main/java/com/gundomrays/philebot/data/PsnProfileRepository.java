package com.gundomrays.philebot.data;

import com.gundomrays.philebot.psn.domain.PsnProfile;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PsnProfileRepository extends CrudRepository<PsnProfile, String> {
    Optional<PsnProfile> findByTgUsername(String tgUsername);
}
