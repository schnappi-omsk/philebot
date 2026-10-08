package com.gundomrays.philebot.telegram.bot;

import com.gundomrays.philebot.command.PhilCommandService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.telegram.telegrambots.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.telegrambots.abilitybots.api.db.DBContext;
import org.telegram.telegrambots.meta.api.methods.reactions.SetMessageReaction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.lang.reflect.Field;
import java.nio.file.Path;

public class PhilBotTest {

    private static final Long CHAT_ID = -100L;

    private static final String MESSAGE_TEXT = "https://x.com/user/status/1 clownword";

    @TempDir
    private Path tempDir;

    private TelegramClient mockTelegramClient;

    private ReactionService mockReactionService;

    private SocialMediaLinkService mockSocialMediaLinkService;

    private PhilCommandService mockPhilCommandService;

    private PhilBot philBot;

    @BeforeEach
    public void setUp() throws Exception {
        mockTelegramClient = Mockito.mock(TelegramClient.class);
        mockReactionService = Mockito.mock(ReactionService.class);
        mockSocialMediaLinkService = Mockito.mock(SocialMediaLinkService.class);
        mockPhilCommandService = Mockito.mock(PhilCommandService.class);

        Mockito.when(mockReactionService.clown()).thenReturn("🤡");

        // Bot username is used as the MapDB file name, so keep the file in the temp directory
        philBot = new PhilBot(mockTelegramClient, tempDir.resolve("philbot-test").toString());

        setField(PhilBot.class, "chatId", CHAT_ID);
        setField(PhilBot.class, "reactionService", mockReactionService);
        setField(PhilBot.class, "socialMediaLinkService", mockSocialMediaLinkService);
        setField(PhilBot.class, "philCommandService", mockPhilCommandService);
    }

    @AfterEach
    public void tearDown() throws Exception {
        Field field = BaseAbilityBot.class.getDeclaredField("db");
        field.setAccessible(true);
        ((DBContext) field.get(philBot)).close();
    }

    @Test
    public void testEditedMessageGetsOnlyClownReaction() throws Exception {
        Message message = textMessage();
        Mockito.when(mockReactionService.needsClownReaction(message)).thenReturn(true);

        philBot.consume(editedUpdate(message));

        Mockito.verify(mockTelegramClient).execute(Mockito.any(SetMessageReaction.class));
        Mockito.verify(mockTelegramClient, Mockito.never()).execute(Mockito.any(SendMessage.class));
        Mockito.verifyNoInteractions(mockSocialMediaLinkService, mockPhilCommandService);
        Mockito.verify(mockReactionService, Mockito.never()).needsManReaction(Mockito.any());
    }

    @Test
    public void testEditedMessageWithoutTriggerIsIgnored() {
        philBot.consume(editedUpdate(textMessage()));

        Mockito.verifyNoInteractions(mockTelegramClient, mockSocialMediaLinkService, mockPhilCommandService);
    }

    @Test
    public void testNewMessageIsProcessed() throws Exception {
        Message message = textMessage();
        Mockito.when(mockSocialMediaLinkService.hasLink(MESSAGE_TEXT)).thenReturn("https://fxtwitter.com/user/status/1");
        Mockito.when(mockReactionService.needsClownReaction(message)).thenReturn(true);

        Update update = Mockito.mock(Update.class);
        Mockito.when(update.getMessage()).thenReturn(message);

        philBot.consume(update);

        Mockito.verify(mockTelegramClient).execute(Mockito.any(SendMessage.class));
        Mockito.verify(mockTelegramClient).execute(Mockito.any(SetMessageReaction.class));
    }

    @Test
    public void testUpdateWithoutMessageIsIgnored() {
        philBot.consume(Mockito.mock(Update.class));

        Mockito.verifyNoInteractions(mockTelegramClient, mockReactionService, mockSocialMediaLinkService, mockPhilCommandService);
    }

    private Message textMessage() {
        User user = Mockito.mock(User.class);
        Mockito.when(user.getUserName()).thenReturn("user");

        Message message = Mockito.mock(Message.class);
        Mockito.when(message.getFrom()).thenReturn(user);
        Mockito.when(message.getText()).thenReturn(MESSAGE_TEXT);
        Mockito.when(message.hasText()).thenReturn(true);
        Mockito.when(message.getChatId()).thenReturn(CHAT_ID);
        Mockito.when(message.getMessageId()).thenReturn(1);
        return message;
    }

    private Update editedUpdate(final Message message) {
        Update update = Mockito.mock(Update.class);
        Mockito.when(update.hasEditedMessage()).thenReturn(true);
        Mockito.when(update.getEditedMessage()).thenReturn(message);
        return update;
    }

    private void setField(final Class<?> clazz, final String name, final Object value) throws Exception {
        Field field = clazz.getDeclaredField(name);
        field.setAccessible(true);
        field.set(philBot, value);
    }
}
