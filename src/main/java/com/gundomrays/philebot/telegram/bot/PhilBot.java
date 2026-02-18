package com.gundomrays.philebot.telegram.bot;

import com.gundomrays.philebot.command.*;
import com.gundomrays.philebot.messaging.MessageQueue;
import com.gundomrays.philebot.telegram.config.SettingsService;
import com.gundomrays.philebot.telegram.exception.TelegramException;
import com.gundomrays.philebot.xbox.domain.Profile;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.reactions.SetMessageReaction;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.games.Animation;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.reactions.ReactionType;
import org.telegram.telegrambots.meta.api.objects.reactions.ReactionTypeEmoji;
import org.telegram.telegrambots.meta.api.objects.stickers.Sticker;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class PhilBot extends AbilityBot {

    private static final Logger log = LoggerFactory.getLogger(PhilBot.class);

    @Value("${messages.congrats}")
    private String congrats;

    @Value("${messages.congrats_sticker}")
    private String congratsSticker;

    @Value("${messages.congrats_emoji}")
    private String congratsEmoji;

    private Long chatId;

    @Autowired
    private PhilCommandService philCommandService;

    @Autowired
    private PeriodicalMessageService periodicalMessageService;

    @Autowired
    private MessageQueue messageQueue;

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private UserActivityService userActivityService;

    @Autowired
    private ReactionService reactionService;

    public PhilBot(TelegramClient telegramClient, String botUsername) {
        super(telegramClient, botUsername);
    }

    @PostConstruct
    private void init() {
        this.chatId = settingsService.chatId();
    }

    @Override
    public void consume(Update update) {
        Message message = update.hasEditedMessage() ? update.getEditedMessage() : update.getMessage();
        User from = message.getFrom();
        String messageText = message.getText();

        initChatId(message);

        if (message.isCommand()) {
            CommandRequest request = parseCommand(message);
            log.info("Command {} was received", request.getCommand());
            PhilCommand command = philCommandService.command(PhilCommandUtils.commandName(request.getCommand()));
            if (command != null) {
                try {
                    final CommandResponse result = command.execute(request);
                    if (isTextResponse(result)) {
                        reply(message.getChatId(), message.getMessageId(), result.getMessage());
                    } else {
                        reply(message.getChatId(), message.getMessageId(), result.getMessage(), result.getMediaUrl());
                    }
                } catch (Exception e) {
                    log.error("Error executing command {} {}", request.getCommand(), request.getArgument());
                    log.error(e.getMessage(), e);
                    reply(message.getChatId(), message.getMessageId(), "<code>Error</code>");
                }
            } else {
                log.info("Command not found: {}", messageText);
                reply(message.getChatId(), message.getMessageId(), "Command not found: " + messageText);
            }
        } else {
            if (message.hasPhoto()) {
                final Random random = new Random();
                if (random.nextInt(100) < 10) {
                    reply(message.getChatId(), message.getMessageId(), "Это ИИ");
                }
            }
            if (message.hasSticker()) {
                Sticker sticker = message.getSticker();
                log.info("Sticker sent by {}, id: {}, unique id: {}, sticker set: {}", from.getUserName(), sticker.getFileId(), sticker.getFileUniqueId(), sticker.getSetName());
                if (reactionService.needsScream(sticker.getSetName())) {
                    sendGif(message.getChatId(), reactionService.screamGif(), message.getMessageId());
                }
            } else {
                log.info("Message from: {}, text: {}, in the chat: {}", from.getUserName(), messageText, message.getChatId());
            }
            if (message.hasAnimation()) {
                Animation gif = message.getAnimation();
                log.info("GIF by {}, id: {}, unique id: {}", from.getUserName(), gif.getFileId(), gif.getFileUniqueId());
            }
            react(message);
            if (message.hasVoice()) {
                reply(message.getChatId(),
                        message.getMessageId(),
                        "",
                        getClass().getClassLoader().getResourceAsStream("media/audio.jpg"), "audio.jpg");
            }
        }
    }

    @Async
    @Scheduled(fixedDelay = 30L, timeUnit = TimeUnit.SECONDS)
    public void retrieveAndSendAchievements() {
        if (chatId == null || chatId == 0) {
            log.warn("Bot needs to be initialized for the chat!");
            return;
        }

        String achievement;
        Message msg = null;
        do {
            achievement = messageQueue.takeMessage();
            if (achievement != null) {
                msg = sendMessage(chatId, achievement);
            }
        } while (achievement != null);

        if (msg != null) {
            final Random random = new Random();

            if (random.nextInt(100) < 10) {
                reply(chatId, msg.getMessageId(), congrats);
                sendSticker(chatId, congratsSticker, congratsEmoji);
            }
        }
    }

    @Scheduled(fixedDelay = 1L, timeUnit = TimeUnit.HOURS)
    public void sendPeriodicalMessage() {
        final Random random = new Random();
        if (chatId != null && random.nextInt(100) < periodicalMessageService.probability()) {
            sendMessage(chatId, periodicalMessageService.message());
        }
    }

    @Scheduled(fixedDelay = 1L, timeUnit = TimeUnit.MINUTES)
    public void updateActiveUsers() {
        Collection<Profile> registeredUsers = userActivityService.registeredUsers();

        for (Profile user : registeredUsers) {
            if (user.isActive() && !presentsInChat(user.getTgId(), chatId)) {
                userActivityService.deactivateUser(user);
                sendMessage(chatId, userActivityService.deactivationMessage(user));
            } else if (!user.isActive() && presentsInChat(user.getTgId(), chatId)) {
                userActivityService.activateUser(user);
                sendMessage(chatId, userActivityService.activationMessage(user));
            }
        }
    }

    public boolean presentsInChat(final Long userId, final Long chatId) {
        final GetChatMember chatMemberGetter = GetChatMember.builder()
                .chatId(chatId)
                .userId(userId)
                .build();
        try {
            final ChatMember chatMember = telegramClient.execute(chatMemberGetter);
            return chatMember != null
                    && !"left".equalsIgnoreCase(chatMember.getStatus())
                    && !"kicked".equalsIgnoreCase(chatMember.getStatus());
        } catch (TelegramApiRequestException e) {
            if (e.getErrorCode() == 400 && e.getMessage().toLowerCase().contains("user not found")) {
                log.warn("Request is failed. userId={}, chatId={}. Probably user is not in chat and have privacy settings.", userId, chatId);
                return false;
            } else {
                throw new TelegramException(e.getMessage(), e);
            }
        } catch (TelegramApiException e) {
            throw new TelegramException(e.getMessage(), e);
        }
    }

    public void react(final Message message) {
        if (message == null) {
            return;
        }
        if (reactionService.needsClownReaction(message)) {
            ReactionType reactionEmoji = ReactionTypeEmoji.builder()
                    .type(ReactionTypeEmoji.EMOJI_TYPE)
                    .emoji(reactionService.clown())
                    .build();
            SetMessageReaction reaction = SetMessageReaction.builder()
                    .chatId(chatId)
                    .messageId(message.getMessageId())
                    .reactionTypes(List.of(reactionEmoji))
                    .build();
            try {
                telegramClient.execute(reaction);
            } catch (TelegramApiException e) {
                throw new TelegramException(e.getMessage(), e);
            }
        }
        if (reactionService.needsManReaction(message)) {
            sendSticker(chatId, reactionService.manSticker(), reactionService.man(), message.getMessageId());
        }
    }

    public void reply(Long chatId, Integer msgId, String text) {
        final SendMessage sender = SendMessage.builder()
                .parseMode(ParseMode.HTML)
                .chatId(chatId)
                .replyToMessageId(msgId)
                .text(text)
                .build();
        try {
            telegramClient.execute(sender);
        } catch (TelegramApiException e) {
            throw new TelegramException(e.getMessage(), e);
        }
    }

    public void reply(Long chatId, Integer msgId, String caption, String photoUrl) {
        final SendPhoto sender = SendPhoto.builder()
                .parseMode(ParseMode.HTML)
                .chatId(chatId)
                .replyToMessageId(msgId)
                .photo(new InputFile(photoUrl))
                .caption(caption)
                .build();
        try {
            telegramClient.execute(sender);
        } catch (TelegramApiException e) {
            throw new TelegramException(e.getMessage(), e);
        }
    }

    public void reply(Long chatId, Integer msgId, String caption, InputStream photo, String fileName) {
        final SendPhoto sender = SendPhoto.builder()
                .parseMode(ParseMode.HTML)
                .chatId(chatId)
                .replyToMessageId(msgId)
                .photo(new InputFile(photo, fileName))
                .caption(caption)
                .build();
        try {
            telegramClient.execute(sender);
        } catch (TelegramApiException e) {
            throw new TelegramException(e.getMessage(), e);
        }
    }

    public Message sendMessage(Long chatId, String text) {
        final SendMessage sender = SendMessage.builder()
                .parseMode(ParseMode.HTML)
                .disableWebPagePreview(false)
                .chatId(chatId)
                .text(text)
                .build();
        try {
            return telegramClient.execute(sender);
        } catch (TelegramApiException e) {
            throw new TelegramException(e.getMessage(), e);
        }
    }

    public void sendSticker(Long chatId, String sticker, String emoji) {
        sendSticker(chatId, sticker, emoji, null);
    }

    public void sendSticker(Long chatId, String sticker, String emoji, Integer replyToId) {
        final SendSticker sender = SendSticker.builder()
                .chatId(chatId)
                .replyToMessageId(replyToId)
                .sticker(new InputFile(sticker))
                .emoji(emoji)
                .build();

        try {
            telegramClient.execute(sender);
        } catch (TelegramApiException e) {
            throw new TelegramException(e.getMessage(), e);
        }
    }

    public void sendGif(Long chatId, String gif, Integer replyToId) {
        final SendAnimation sender = SendAnimation.builder()
                .chatId(chatId)
                .replyToMessageId(replyToId)
                .animation(new InputFile(gif))
                .build();
        try {
            telegramClient.execute(sender);
        } catch (TelegramApiException e) {
            throw new TelegramException(e.getMessage(), e);
        }
    }

    private void initChatId(final Message message) {
        if (chatId == null || chatId == 0) {
            final Long chatId = message.getChatId();
            this.chatId = settingsService.chatId(String.valueOf(chatId));
        }
    }

    private boolean isTextResponse(final CommandResponse response) {
        return response.getMediaUrl() == null || response.getMediaUrl().isEmpty();
    }


    private CommandRequest parseCommand(final Message msg) {
        final CommandRequest request = new CommandRequest();
        request.setChatId(msg.getChatId());
        request.setCaller(msg.getFrom().getUserName());
        request.setCallerId(msg.getFrom().getId());

        final String messageText = msg.getText();
        int spaceIndex = messageText.indexOf(" ");

        if (spaceIndex > -1) {
            request.setCommand(messageText.substring(0, spaceIndex));
            request.setArgument(messageText.substring(spaceIndex).trim());
        } else {
            final String[] commandParts = messageText.split("(?<=\\D)(?=\\d)");
            if (commandParts.length == 1) {
                request.setCommand(messageText);
            } else {
                request.setCommand(commandParts[0]);
                request.setArgument(commandParts[1]);
            }
        }
        return request;
    }

    @Override
    public long creatorId() {
        return 112639296;
    }

}