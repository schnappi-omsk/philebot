package com.gundomrays.philebot.telegram.bot;

import com.gundomrays.philebot.telegram.bot.transform.MessageTransformer;
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

    private final MessageTransformer cyrillicLowerCaseTransformer;

    public ReactionService(MessageTransformer cyrillicLowerCaseTransformer) {
        this.cyrillicLowerCaseTransformer = cyrillicLowerCaseTransformer;
    }

    @PostConstruct
    private void init() {
        clownTriggerWords = new HashSet<>(Arrays.asList(clownTriggers.split(",")));
    }

    public boolean needsClownReaction(final String messageText) {
        return messageText != null && clownTriggerWords.stream()
                .anyMatch(value -> cyrillicLowerCaseTransformer.transform(messageText).toLowerCase().contains(value.toLowerCase()));
    }

    public String clown() {
        return clownEmoji;
    }

}
