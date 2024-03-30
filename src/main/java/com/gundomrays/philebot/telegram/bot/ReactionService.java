package com.gundomrays.philebot.telegram.bot;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
public class ReactionService {

    @Value("${messages.react.clown.emoji}")
    private String clownEmoji;

    @Value("${messages.react.clown.triggers}")
    private String clownTriggers;

    private Set<String> clownTriggerWords = new HashSet<>();

    @PostConstruct
    private void init() {
        clownTriggerWords = new HashSet<>(Arrays.asList(clownTriggers.split(",")));
    }

    public boolean needsClownReaction(final String messageText) {
        return clownTriggerWords.stream()
                .anyMatch(value -> messageText.toLowerCase().contains(value.toLowerCase()));
    }

    public String clown() {
        return clownEmoji;
    }

}
