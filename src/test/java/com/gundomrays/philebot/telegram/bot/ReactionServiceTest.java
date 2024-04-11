package com.gundomrays.philebot.telegram.bot;

import com.gundomrays.philebot.telegram.bot.transform.MessageTransformer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReactionServiceTest {

    @Mock
    private MessageTransformer mockTransformer;

    private ReactionService reactionService;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        reactionService = new ReactionService(mockTransformer);

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
}