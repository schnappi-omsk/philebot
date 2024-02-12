package com.gundomrays.philebot.data;

import com.gundomrays.philebot.xbox.domain.Help;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class HelpDataService {

    private final HelpRepository helpRepository;

    public HelpDataService(HelpRepository helpRepository) {
        this.helpRepository = helpRepository;
    }

    public Collection<Help> help() {
        return StreamSupport.stream(helpRepository.findAll().spliterator(), false).toList();
    }

}
