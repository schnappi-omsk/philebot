package com.gundomrays.philebot.telegram.bot;

import com.gundomrays.philebot.ai.ChatGptClient;
import com.gundomrays.philebot.telegram.bot.transform.MessageTransformer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.richblock.*;
import org.telegram.telegrambots.meta.api.objects.richtext.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReactionServiceTest {

    @Mock
    private MessageTransformer mockTransformer;

    private ChatGptClient mockChatGptClient;

    private ReactionService reactionService;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        reactionService = new ReactionService(mockTransformer, mockChatGptClient);

        Set<String> clownTriggerWords = new HashSet<>(Arrays.asList("clownword", "clow0word", "русня"));

        // Use reflection to access the private field
        Field field = reactionService.getClass().getDeclaredField("clownTriggerWords");
        field.setAccessible(true);
        field.set(reactionService, clownTriggerWords);
    }

    @Test
    public void testNeedsClownReactionWhenContainsClownTrigger() {
        Mockito.when(mockTransformer.transform(Mockito.any())).thenReturn("clownword");

        assertTrue(reactionService.needsClownReaction("Random message with clownword"));
    }

    @Test
    public void testNeedsClownReactionWhenContainsObfuscatedClownTrigger() {
        Mockito.when(mockTransformer.transform(Mockito.any())).thenReturn("clow0word");

        assertTrue(reactionService.needsClownReaction("Random message with obfuscated clown Trigger clow0word"));
    }

    @Test
    public void testNeedsClownReactionWhenContainsNonTriggerWord() {
        Mockito.when(mockTransformer.transform(Mockito.any())).thenReturn("NoTriggerWord");

        assertFalse(reactionService.needsClownReaction("Random message with Non Trigger Word"));
    }

    @Test
    public void testNeedsClownReactionWhenArticleParagraphContainsClownTrigger() {
        Mockito.when(mockTransformer.transform(Mockito.any())).thenAnswer(inv -> inv.getArgument(0));

        RichMessage article = new RichMessage(List.of(
                RichBlockSectionHeading.builder().text(new RichTextPlain("Some heading")).size(1).build(),
                new RichBlockParagraph(new RichTextPlain("Article paragraph with clownword inside"))
        ));

        assertTrue(reactionService.needsClownReaction(articleMessage(article)));
    }

    @Test
    public void testNeedsClownReactionWhenArticleListItemContainsNestedClownTrigger() {
        Mockito.when(mockTransformer.transform(Mockito.any())).thenAnswer(inv -> inv.getArgument(0));

        RichText formatted = new RichTextConcat(List.of(
                new RichTextPlain("List item with "),
                new RichTextBold(new RichTextPlain("clownword")),
                new RichTextPlain(" in bold")
        ));
        RichMessage article = new RichMessage(List.of(
                new RichBlockList(List.of(new RichBlockListItem("1.", List.of(new RichBlockParagraph(formatted)))))
        ));

        assertTrue(reactionService.needsClownReaction(articleMessage(article)));
    }

    @Test
    public void testNeedsClownReactionWhenArticlePhotoCaptionContainsClownTrigger() {
        Mockito.when(mockTransformer.transform(Mockito.any())).thenAnswer(inv -> inv.getArgument(0));

        RichMessage article = new RichMessage(List.of(
                new RichBlockParagraph(new RichTextPlain("Nothing special here")),
                new RichBlockPhoto(List.of(), false, new RichBlockCaption(new RichTextPlain("Photo of clownword"), null))
        ));

        assertTrue(reactionService.needsClownReaction(articleMessage(article)));
    }

    @Test
    public void testNeedsClownReactionWhenArticleFormulaBlockContainsClownTrigger() {
        Mockito.when(mockTransformer.transform(Mockito.any())).thenAnswer(inv -> inv.getArgument(0));

        RichMessage article = new RichMessage(List.of(
                new RichBlockParagraph(new RichTextPlain("Nothing special here")),
                new RichBlockMathematicalExpression("x = clownword^2")
        ));

        assertTrue(reactionService.needsClownReaction(articleMessage(article)));
    }

    @Test
    public void testNeedsClownReactionWhenArticleInlineFormulaContainsClownTrigger() {
        Mockito.when(mockTransformer.transform(Mockito.any())).thenAnswer(inv -> inv.getArgument(0));

        RichText formatted = new RichTextConcat(List.of(
                new RichTextPlain("Inline formula "),
                new RichTextMathematicalExpression("y = clownword + 1")
        ));
        RichMessage article = new RichMessage(List.of(new RichBlockParagraph(formatted)));

        assertTrue(reactionService.needsClownReaction(articleMessage(article)));
    }

    @Test
    public void testNeedsClownReactionWhenArticleContainsNonTriggerWords() {
        Mockito.when(mockTransformer.transform(Mockito.any())).thenAnswer(inv -> inv.getArgument(0));

        RichMessage article = new RichMessage(List.of(
                RichBlockSectionHeading.builder().text(new RichTextPlain("Weekly news")).size(1).build(),
                new RichBlockParagraph(new RichTextPlain("Absolutely harmless article text"))
        ));

        assertFalse(reactionService.needsClownReaction(articleMessage(article)));
    }

    @Test
    public void testNeedsClownReactionWhenArticleHasNoTextBlocks() {
        Mockito.when(mockTransformer.transform(Mockito.any())).thenAnswer(inv -> inv.getArgument(0));

        RichMessage article = new RichMessage(Arrays.asList(new RichBlockDivider(), null));

        assertFalse(reactionService.needsClownReaction(articleMessage(article)));
    }

    @Test
    public void testNeedsClownReactionForTextMessageIsNotAffectedByArticleSupport() {
        Mockito.when(mockTransformer.transform(Mockito.any())).thenAnswer(inv -> inv.getArgument(0));

        Message message = Mockito.mock(Message.class);
        Mockito.when(message.getText()).thenReturn("Plain message with clownword");

        assertTrue(reactionService.needsClownReaction(message));
    }

    @Test
    public void testNeedsManReactionIsNotTriggeredByArticle() throws Exception {
        Field field = reactionService.getClass().getDeclaredField("manTriggerWords");
        field.setAccessible(true);
        field.set(reactionService, new HashSet<>(List.of("manword")));

        RichMessage article = new RichMessage(List.of(
                new RichBlockParagraph(new RichTextPlain("Article with manword inside"))
        ));

        assertFalse(reactionService.needsManReaction(articleMessage(article)));
    }

    private Message articleMessage(final RichMessage article) {
        Message message = Mockito.mock(Message.class);
        Mockito.when(message.getRichMessage()).thenReturn(article);
        return message;
    }
}