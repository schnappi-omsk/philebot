package com.gundomrays.philebot.telegram.bot;

import com.gundomrays.philebot.command.CommandRequest;
import com.gundomrays.philebot.command.PhilCommand;
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
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Collection;
import java.util.Map;
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
    private Map<String, PhilCommand> commands;

    @Autowired
    private PhilAchievementRetriever philAchievementRetriever;

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
            PhilCommand command = commands.get(request.getCommand());
            if (command != null) {
                final String result = command.execute(request);
                reply(message.getChatId(), message.getMessageId(), result);
            } else {
                log.info("Command not found: {}", messageText);
                reply(message.getChatId(), message.getMessageId(), "Command not found: " + messageText);
            }
        }
//        else {
//            reply(message.getChatId(), message.getMessageId(), systemArg);
//        }
    }

    @Async
    @Scheduled(fixedDelay = 1L, timeUnit = TimeUnit.MINUTES)
    public void retrieveAndSendAchievements() {
        log.info("Start getting achievements");
        Collection<String> achievements = philAchievementRetriever.retrieve();

        for (final String achievement : achievements) {
            sendAchievementMsg(chatId, achievement);
        }
    }

    public void reply(Long chatId, Integer msgId, String text) {
        final SendMessage sender = SendMessage.builder()
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
    public void sendAchievementMsg(Long chatId, String text) {
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


    private CommandRequest parseCommand(Message msg) {
        final CommandRequest request = new CommandRequest();
        request.setChatId(msg.getChatId());
        request.setCaller(msg.getFrom().getUserName());

        final String messageText = msg.getText();
        int spaceIndex = messageText.indexOf(" ");
        final String command = spaceIndex > -1 ? messageText.substring(0, spaceIndex) : messageText;
        request.setCommand(command);
        if (spaceIndex > -1) {
            request.setArgument(messageText.substring(spaceIndex).trim());
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
