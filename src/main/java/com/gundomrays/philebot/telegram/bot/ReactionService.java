package com.gundomrays.philebot.telegram.bot;

import com.google.common.base.CharMatcher;
import com.gundomrays.philebot.ai.ChatGptClient;
import com.gundomrays.philebot.telegram.bot.transform.MessageTransformer;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.richblock.*;
import org.telegram.telegrambots.meta.api.objects.richtext.*;
import org.telegram.telegrambots.meta.api.objects.stickers.Sticker;

import java.io.InputStream;
import java.net.URLConnection;
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

    @Value("${messages.react.clown.sticker.uniqueId}")
    private String clownSticker;

    private Set<String> clownTriggerWords = new HashSet<>();

    private Set<String> manTriggerWords = new HashSet<>();

    private Set<String> manExceptionWords = new HashSet<>();

    private final MessageTransformer cyrillicLowerCaseTransformer;

    private final ChatGptClient chatGptClient;

    public ReactionService(MessageTransformer cyrillicLowerCaseTransformer, ChatGptClient chatGptClient) {
        this.cyrillicLowerCaseTransformer = cyrillicLowerCaseTransformer;
        this.chatGptClient = chatGptClient;
    }

    @PostConstruct
    private void init() {
        clownTriggerWords = new HashSet<>(Arrays.asList(clownTriggers.split(",")));
        manTriggerWords = new HashSet<>(Arrays.asList(manTriggers.split(",")));
        manExceptionWords = new HashSet<>(Arrays.asList(manExceptions.split(",")));
    }

    public boolean needsClownReaction(final Message message) {
        if (message.hasSticker()) {
            return checkSticker(message.getSticker());
        }
        if (message.getRichMessage() != null) {
            return needsClownReaction(message.getRichMessage());
        }
        return needsClownReaction(message.getText());
    }

    public boolean needsClownReaction(final RichMessage article) {
        return needsClownReaction(articleText(article));
    }

    public boolean needsClownReaction(final String filePath, final InputStream fileAsStream) {
        String mimeType = URLConnection.guessContentTypeFromName(filePath);
        if (mimeType == null) {
            mimeType = "image/jpeg";
        }
        String text = chatGptClient.extractTextFromImage(fileAsStream, mimeType);
        return needsClownReaction(text);
    }

    public boolean needsClownReaction(final String messageText) {
        return checkMsgText(messageText);
    }

    public String clown() {
        return clownEmoji;
    }

    public boolean needsManReaction(final Message message) {
        final String messageText = message.getText();
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

    private boolean checkMsgText(final String messageText) {
        final String transformed = cyrillicLowerCaseTransformer.transform(messageText);
        final boolean contains = containsClownTrigger(transformed);

        final List<String> words = words(transformed);
        final boolean obfuscated = containsObfuscatedClownTrigger(words);
        final boolean divided = containsDividedClownTrigger(words);
        final boolean splitted = isSplitted(words);

        return contains || obfuscated || divided || splitted;
    }

    private boolean checkSticker(final Sticker sticker) {
        final String id = sticker.getFileUniqueId();
        return clownSticker.equals(id);
    }

    private String articleText(final RichMessage article) {
        return article == null ? "" : blocksText(article.getBlocks());
    }

    private String blocksText(final List<RichBlock> blocks) {
        if (blocks == null) {
            return "";
        }
        return joinTexts(blocks.stream().map(this::blockText).toList());
    }

    private String blockText(final RichBlock block) {
        return switch (block) {
            case RichBlockParagraph b -> richText(b.getText());
            case RichBlockSectionHeading b -> richText(b.getText());
            case RichBlockPreformatted b -> richText(b.getText());
            case RichBlockFooter b -> richText(b.getText());
            case RichBlockThinking b -> richText(b.getText());
            case RichBlockMathematicalExpression b -> StringUtils.defaultString(b.getExpression());
            case RichBlockPullQuotation b -> joinTexts(List.of(richText(b.getText()), richText(b.getCredit())));
            case RichBlockBlockQuotation b -> joinTexts(List.of(blocksText(b.getBlocks()), richText(b.getCredit())));
            case RichBlockDetails b -> joinTexts(List.of(richText(b.getSummary()), blocksText(b.getBlocks())));
            case RichBlockList b -> b.getItems() == null ? "" : joinTexts(b.getItems().stream()
                    .map(item -> item == null ? "" : blocksText(item.getBlocks()))
                    .toList());
            case RichBlockTable b -> joinTexts(List.of(tableText(b.getCells()), richText(b.getCaption())));
            case RichBlockCollage b -> joinTexts(List.of(blocksText(b.getBlocks()), captionText(b.getCaption())));
            case RichBlockSlideshow b -> joinTexts(List.of(blocksText(b.getBlocks()), captionText(b.getCaption())));
            case RichBlockPhoto b -> captionText(b.getCaption());
            case RichBlockVideo b -> captionText(b.getCaption());
            case RichBlockAnimation b -> captionText(b.getCaption());
            case RichBlockAudio b -> captionText(b.getCaption());
            case RichBlockVoiceNote b -> captionText(b.getCaption());
            case RichBlockMap b -> captionText(b.getCaption());
            case null, default -> "";
        };
    }

    private String tableText(final List<List<RichBlockTableCell>> cells) {
        if (cells == null) {
            return "";
        }
        return joinTexts(cells.stream()
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .map(cell -> cell == null ? "" : richText(cell.getText()))
                .toList());
    }

    private String captionText(final RichBlockCaption caption) {
        return caption == null ? "" : joinTexts(List.of(richText(caption.getText()), richText(caption.getCredit())));
    }

    private String richText(final RichText text) {
        return switch (text) {
            case RichTextPlain t -> StringUtils.defaultString(t.getText());
            case RichTextConcat t -> t.getTexts() == null ? "" : t.getTexts().stream()
                    .map(this::richText)
                    .collect(Collectors.joining());
            case RichTextCustomEmoji t -> StringUtils.defaultString(t.getAlternativeText());
            case RichTextMathematicalExpression t -> StringUtils.defaultString(t.getExpression());
            case RichTextBold t -> richText(t.getText());
            case RichTextItalic t -> richText(t.getText());
            case RichTextUnderline t -> richText(t.getText());
            case RichTextStrikethrough t -> richText(t.getText());
            case RichTextSpoiler t -> richText(t.getText());
            case RichTextCode t -> richText(t.getText());
            case RichTextMarked t -> richText(t.getText());
            case RichTextSubscript t -> richText(t.getText());
            case RichTextSuperscript t -> richText(t.getText());
            case RichTextUrl t -> richText(t.getText());
            case RichTextAnchorLink t -> richText(t.getText());
            case RichTextReference t -> richText(t.getText());
            case RichTextReferenceLink t -> richText(t.getText());
            case RichTextMention t -> richText(t.getText());
            case RichTextTextMention t -> richText(t.getText());
            case RichTextHashtag t -> richText(t.getText());
            case RichTextCashtag t -> richText(t.getText());
            case RichTextBotCommand t -> richText(t.getText());
            case RichTextEmailAddress t -> richText(t.getText());
            case RichTextPhoneNumber t -> richText(t.getText());
            case RichTextBankCardNumber t -> richText(t.getText());
            case RichTextDateTime t -> richText(t.getText());
            case null, default -> "";
        };
    }

    private String joinTexts(final List<String> texts) {
        return texts.stream()
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(" "));
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
