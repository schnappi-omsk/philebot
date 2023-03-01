package com.gundomrays.philebot.data;

import com.gundomrays.philebot.xbox.domain.Profile;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface XboxProfileRepository extends CrudRepository<Profile, String> {
    Optional<Profile> findByGamertag(String gamertag);
}
