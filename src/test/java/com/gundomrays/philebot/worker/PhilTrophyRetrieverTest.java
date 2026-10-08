package com.gundomrays.philebot.worker;

import com.gundomrays.philebot.messaging.MessageQueue;
import com.gundomrays.philebot.psn.PsnTrophyQueue;
import com.gundomrays.philebot.psn.domain.PsnProfile;
import com.gundomrays.philebot.psn.domain.PsnTrophy;
import com.gundomrays.philebot.psn.domain.PsnTrophyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class PhilTrophyRetrieverTest {

    private PsnTrophyQueue trophyQueue;

    private MessageQueue mockMessageQueue;

    private PhilTrophyRetriever retriever;

    private PsnProfile player;

    @BeforeEach
    public void setUp() throws Exception {
        trophyQueue = new PsnTrophyQueue();
        mockMessageQueue = Mockito.mock(MessageQueue.class);
        retriever = new PhilTrophyRetriever(trophyQueue, mockMessageQueue);

        Field field = PhilTrophyRetriever.class.getDeclaredField("serviceHost");
        field.setAccessible(true);
        field.set(retriever, "https://bot.example.com");

        player = new PsnProfile();
        player.setOnlineId("test_player");
        player.setTgUsername("tg_player");
        player.setTgId(42L);
    }

    @Test
    public void testQueueIsDrainedIntoMessages() {
        trophyQueue.placeTrophy(trophy("Ratchet & Clank", PsnTrophyType.GOLD));
        trophyQueue.placeTrophy(trophy("ASTRO's PLAYROOM", PsnTrophyType.PLATINUM));

        retriever.retrieve();

        Mockito.verify(mockMessageQueue, Mockito.times(2)).messageToSend(Mockito.anyString());
        assertNull(trophyQueue.takeTrophy());
    }

    @Test
    public void testMessageIsEscapedAndLinksToCard() {
        trophyQueue.placeTrophy(trophy("Ratchet & Clank <Rift>", PsnTrophyType.GOLD));

        retriever.retrieve();

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(mockMessageQueue).messageToSend(captor.capture());
        String message = captor.getValue();
        assertTrue(message.startsWith("<code>tg_player</code> — 🥇 <a href='https://bot.example.com/psn/card?name=Hidden+%3CHero%3E&desc=Find+the+secret+%26+win.&type=Gold&rarity=3.7&img=https%3A%2F%2Fimage.example.com%2F2.png&seed="));
        assertTrue(message.endsWith("'>Ratchet &amp; Clank &lt;Rift&gt;</a>"));
    }

    @Test
    public void testPlayerIsPinged() {
        player.setPing(true);

        String message = retriever.playerTrophyText(trophy("Game", PsnTrophyType.PLATINUM));

        assertTrue(message.startsWith("<a href='tg://user?id=42'>@tg_player</a> — 🏆 "));
    }

    private PsnTrophy trophy(final String titleName, final PsnTrophyType type) {
        return new PsnTrophy(player, titleName, "Hidden <Hero>", "Find the secret & win.",
                "https://image.example.com/2.png", type, "3.7", Instant.parse("2026-10-01T21:58:18Z"));
    }
}
