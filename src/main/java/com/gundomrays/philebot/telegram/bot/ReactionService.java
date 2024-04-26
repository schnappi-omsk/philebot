package com.gundomrays.philebot.telegram.bot;

import com.google.common.base.CharMatcher;
import com.gundomrays.philebot.telegram.bot.transform.MessageTransformer;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReactionService {

    @Value("${messages.react.clown.emoji}")
    private String clownEmoji;

    @Value("${messages.react.clown.triggers}")
    private String clownTriggers;

    @Value("${messages.react.custom.man.emoji}")
    private String manEmoji;

    @Value("${messages.react.custom.man.sticker}")
    private String manSticker;

    @Value("${messages.react.custom.man.triggers}")
    private String manTriggers;

    private Set<String> clownTriggerWords = new HashSet<>();

    private Set<String> manTriggerWords = new HashSet<>();

    private final MessageTransformer cyrillicLowerCaseTransformer;

    public ReactionService(MessageTransformer cyrillicLowerCaseTransformer) {
        this.cyrillicLowerCaseTransformer = cyrillicLowerCaseTransformer;
    }

    @PostConstruct
    private void init() {
        clownTriggerWords = new HashSet<>(Arrays.asList(clownTriggers.split(",")));
        manTriggerWords = new HashSet<>(Arrays.asList(manTriggers.split(",")));
    }

    public boolean needsClownReaction(final String messageText) {
        final String transformed = cyrillicLowerCaseTransformer.transform(messageText);
        final boolean contains = containsClownTrigger(transformed);
        final boolean obfuscated = containsObfuscatedClownTrigger(transformed);

        return contains || obfuscated;
    }

    public String clown() {
        return clownEmoji;
    }

    public boolean needsManReaction(final String messageText) {
        return manTriggerWords.stream().anyMatch(messageText::contains);
    }

    public String man() {
        return manEmoji;
    }

    public String manSticker() {
        return manSticker;
    }

    private Set<String> words(final String text) {
        return Arrays.stream(text.split("\\R"))
                .flatMap(line -> Arrays.stream(line.split("\\s+")))
                .collect(Collectors.toSet());
    }

    private boolean containsObfuscatedClownTrigger(final String text) {
        final Set<String> words = words(text);
        for (final String word : words) {
            for (final String clownWord : clownTriggerWords) {
                if (isObfuscation(word, clownWord, 1)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean containsClownTrigger(final String text) {
        return text != null && clownTriggerWords.stream()
                .anyMatch(value ->
                        cyrillicLowerCaseTransformer.transform(text).toLowerCase().contains(value.toLowerCase())
                );
    }

    private boolean isObfuscation(final String value, final String listed, final int threshold) {
        final String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        final String retained = CharMatcher.javaLetterOrDigit().retainFrom(normalized);
        final int distance = StringUtils.getLevenshteinDistance(retained, listed);
        return distance <= threshold;
    }

}
