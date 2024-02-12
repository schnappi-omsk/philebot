package com.gundomrays.philebot.telegram.data;

import com.gundomrays.philebot.telegram.domain.Settings;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettingsRepository extends CrudRepository<Settings, String> {
}
