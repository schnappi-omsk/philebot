package com.gundomrays.philebot.telegram.bot;

import com.gundomrays.philebot.command.*;
import com.gundomrays.philebot.telegram.exception.TelegramException;
import com.gundomrays.philebot.worker.PhilAchievementRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class PhilBot extends TelegramLongPollingBot {

    private static final Logger log = LoggerFactory.getLogger(PhilBot.class);

    @Value("${tg.botName}")
    private String botName;

    @Value("${tg.systemCaller}")
    private String systemCaller;

    @Value("${tg.systemArg}")
    private String systemArg;

    @Value("${tg.chatId}")
    private Long chatId;

    @Autowired
    private PhilCommandService philCommandService;

    @Autowired
    private PhilAchievementRetriever philAchievementRetriever;

    @Autowired
    private PeriodicalMessageService periodicalMessageService;

    public PhilBot(String botToken) {
        super(botToken);
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        User from = message.getFrom();
        String messageText = message.getText();
        log.info("Message from: {}, text: {}, in the chat: {}", from.getUserName(), messageText, message.getChatId());

        if (message.isCommand()) {
            CommandRequest request = parseCommand(message);
            log.info("Command {} was received", request.getCommand());
            PhilCommand command = philCommandService.command(PhilCommandUtils.commandName(request.getCommand()));
            if (command != null) {
                final CommandResponse result = command.execute(request);
                if (isTextResponse(result)) {
                    reply(message.getChatId(), message.getMessageId(), result.getMessage());
                } else {
                    reply(message.getChatId(), message.getMessageId(), result.getMessage(), result.getMediaUrl());
                }
            } else {
                log.info("Command not found: {}", messageText);
                reply(message.getChatId(), message.getMessageId(), "Command not found: " + messageText);
            }
        }
    }

    @Async
    @Scheduled(fixedDelay = 5L, timeUnit = TimeUnit.MINUTES)
    public void retrieveAndSendAchievements() {
        log.info("Start getting achievements");
        Collection<String> achievements = philAchievementRetriever.retrieve();

        for (final String achievement : achievements) {
            sendMessage(chatId, achievement);
        }
    }

    @Scheduled(fixedDelay = 1L, timeUnit = TimeUnit.HOURS)
    public void sendPeriodicalMessage() {
        final Random random = new Random();
        if (random.nextInt(100) < 3) {
            sendMessage(chatId, periodicalMessageService.message());
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
            execute(sender);
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
            execute(sender);
        } catch (TelegramApiException e) {
            throw new TelegramException(e.getMessage(), e);
        }
    }

    public void sendMessage(Long chatId, String text) {
        final SendMessage sender = SendMessage.builder()
                .parseMode(ParseMode.HTML)
                .disableWebPagePreview(false)
                .chatId(chatId)
                .text(text)
                .build();
        try {
            execute(sender);
        } catch (TelegramApiException e) {
            throw new TelegramException(e.getMessage(), e);
        }
    }

    private boolean isTextResponse(final CommandResponse response) {
        return response.getMediaUrl() == null || response.getMediaUrl().isEmpty();
    }


    private CommandRequest parseCommand(Message msg) {
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

    private CommandRequest systemRequest() {
        final CommandRequest request = new CommandRequest();
        request.setCaller(systemCaller);
        request.setArgument(systemArg);
        request.setChatId(chatId);
        return request;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

}