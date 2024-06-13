package com.gundomrays.philebot.telegram.bot;

import com.google.common.base.CharMatcher;
import com.gundomrays.philebot.telegram.bot.transform.MessageTransformer;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Comparator;
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

    @Value("${messages.react.custom.man.exceptions}")
    private String manExceptions;

    private Set<String> clownTriggerWords = new HashSet<>();

    private Set<String> manTriggerWords = new HashSet<>();

    private Set<String> manExceptionWords = new HashSet<>();

    private final MessageTransformer cyrillicLowerCaseTransformer;

    public ReactionService(MessageTransformer cyrillicLowerCaseTransformer) {
        this.cyrillicLowerCaseTransformer = cyrillicLowerCaseTransformer;
    }

    @PostConstruct
    private void init() {
        clownTriggerWords = new HashSet<>(Arrays.asList(clownTriggers.split(",")));
        manTriggerWords = new HashSet<>(Arrays.asList(manTriggers.split(",")));
        manExceptionWords = new HashSet<>(Arrays.asList(manExceptions.split(",")));
    }

    public boolean needsClownReaction(final String messageText) {
        final String transformed = cyrillicLowerCaseTransformer.transform(messageText);
        final boolean contains = containsClownTrigger(transformed);

        final Set<String> words = words(transformed);
        final boolean obfuscated = containsObfuscatedClownTrigger(words);
        final boolean divided = containsDividedClownTrigger(words);
        final boolean splitted = isSplitted(words);

        return contains || obfuscated || divided || splitted;
    }

    public String clown() {
        return clownEmoji;
    }

    public boolean needsManReaction(final String messageText) {
        if (messageText == null) {
            return false;
        }

        HashSet<String> messageWords = new HashSet<>(Arrays.asList(messageText.toLowerCase().split(" ")));

        for (final String word : messageWords) {
            boolean triggered = manTriggerWords.stream().map(String::toLowerCase).anyMatch(word::contains);
            if (triggered) {
                boolean noException = manExceptionWords.stream().map(String::toLowerCase).noneMatch(word::contains);
                if (noException) {
                    return true;
                }
            }
        }

        return false;
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

    private boolean containsDividedClownTrigger(Set<String> words) {
        for (final String word : words) {
            for (final String clownWord : clownTriggerWords) {
                if (isDivided(word, clownWord)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isDivided(String word, String clownWord) {
        return word.replaceAll("[^\\p{L}]", "")
                .toLowerCase()
                .contains(clownWord.toLowerCase());
    }

    private boolean containsObfuscatedClownTrigger(final Set<String> words) {
        for (final String word : words) {
            for (final String clownWord : clownTriggerWords) {
                if (isObfuscation(word, clownWord, 1)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isSplitted(Set<String> words) {
        final int maxTriggerLength = clownTriggerWords.stream().map(String::length)
                .max(Comparator.naturalOrder()).orElse(0);
        final StringBuilder shortWords = new StringBuilder();

        for (final String word : words) {
            if (word.length() < maxTriggerLength) {
                shortWords.append(word);
            }
        }

        return containsClownTrigger(shortWords.toString());
    }

    private boolean containsClownTrigger(final String text) {
        return text != null && clownTriggerWords.stream()
                .anyMatch(value ->
                        cyrillicLowerCaseTransformer.transform(text).toLowerCase().contains(value.toLowerCase())
                );
    }

    private boolean isObfuscation(final String value, final String listed, final int threshold) {
        final String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        final String noCombiningChars = normalized.replaceAll("\\p{M}", "");
        final String retained = CharMatcher.javaLetterOrDigit().retainFrom(noCombiningChars);
        final int distance = StringUtils.getLevenshteinDistance(retained, listed);
        return distance <= threshold;
    }

}
