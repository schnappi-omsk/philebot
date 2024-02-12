package com.gundomrays.philebot.data;

import com.gundomrays.philebot.xbox.domain.Help;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface HelpRepository extends CrudRepository<Help, String> {
}
