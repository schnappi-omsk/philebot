package com.gundomrays.philebot.telegram.bot;

import com.google.common.base.CharMatcher;
import com.gundomrays.philebot.telegram.bot.transform.MessageTransformer;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ReactionService {

    private static final Map<Character, Character> controlCharacterPairs = Map.ofEntries(
            Map.entry('(', ')'),
            Map.entry('{', '}'),
            Map.entry('[', ']'),
            Map.entry('<', '>'),
            Map.entry('«', '»'),
            Map.entry('｛', '｝'),
            Map.entry('［', '］'),
            Map.entry('（', '）'),
            Map.entry('＜', '＞'),
            Map.entry('【', '】'),
            Map.entry('〖', '〗'),
            Map.entry('《', '》'),
            Map.entry('「', '」')
    );

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

    @Value("${messages.scream_sets}")
    private Set<String> screamSets;

    @Value("${messages.scream_gif}")
    private String screamGif;

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

        final List<String> words = words(transformed);
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

    public boolean needsScream(final String stickerSetName) {
        return screamSets != null && screamSets.contains(stickerSetName);
    }

    public String screamGif() {
        return screamGif;
    }

    public String man() {
        return manEmoji;
    }

    public String manSticker() {
        return manSticker;
    }

    private List<String> words(final String text) {
        return Arrays.stream(text.split("\\R"))
                .flatMap(line -> Arrays.stream(line.split("\\s+")))
                .collect(Collectors.toList());
    }

    private boolean containsDividedClownTrigger(List<String> words) {
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

    private boolean containsObfuscatedClownTrigger(final List<String> words) {
        for (final String word : words) {
            if (containsObfuscatedClownTrigger(word)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsObfuscatedClownTrigger(String word) {
        for (final String clownWord : clownTriggerWords) {
            int threshold = 1;
            if (word.length() <= clownWord.length()) {
                final int nonLetterChars = nonLettersCount(word);
                final boolean needToUpdateThreshold = nonLetterChars > 0 && nonLetterChars < word.length() / 2;
                if (needToUpdateThreshold) {
                    threshold = word.length() / 2;
                }
            }
            if (isObfuscation(word, clownWord, threshold)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSplitted(List<String> words) {
        final int maxTriggerLength = clownTriggerWords.stream().map(String::length)
                .max(Comparator.naturalOrder()).orElse(0);
        final StringBuilder shortWords = new StringBuilder();

        for (final String word : words) {
            if (word.length() < maxTriggerLength) {
                shortWords.append(word);
            }
        }

        String resultText = shortWords.toString();
        return containsClownTrigger(resultText) || containsObfuscatedClownTrigger(resultText);
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
        final String retained = CharMatcher.javaLetterOrDigit().retainFrom(noCombiningChars).replaceAll("\\p{Punct}", "");
        final int distance = StringUtils.getLevenshteinDistance(retained, listed);
        return retained.length() == listed.length() && distance <= threshold;
    }

    private int nonLettersCount(final String input) {
        final String word = isQuoted(input) ? input.substring(1, input.length() - 1) : input;
        final Pattern pattern = Pattern.compile("[^\\p{L}]");
        final Matcher matcher = pattern.matcher(word);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private boolean isQuoted(final String input) {
        if (input == null || input.length() < 3) {
            return false;
        }

        final String stdQuotes = "\"\"''";

        final String str = input.trim();
        char firstChar = str.charAt(0);
        char lastChar = str.charAt(str.length() - 1);
        if (stdQuotes.contains(String.valueOf(firstChar)) || stdQuotes.contains(String.valueOf(lastChar))) {
            return true;
        }

        return controlCharacterPairs.get(firstChar) != null && controlCharacterPairs.get(firstChar) == lastChar;
    }
}
