package com.gundomrays.philebot.telegram.bot;

import com.gundomrays.philebot.command.CommandRequest;
import com.gundomrays.philebot.command.PhilCommand;
import com.gundomrays.philebot.command.SystemCommandTypes;
import com.gundomrays.philebot.telegram.exception.TelegramException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@EnableAsync
@EnableScheduling
public class PhilBot extends TelegramLongPollingBot {

    private static final Logger log = LoggerFactory.getLogger(PhilBot.class);

    @Value("${tg.botName}")
    private String botName;

    @Value("${tg.systemCaller}")
    private String systemCaller;

    @Value("${tg.systemArg}")
    private String systemArg;

    @Autowired
    private Map<String, PhilCommand> commands;

    public PhilBot(String botToken) {
        super(botToken);
    }

    @Override
    @Async
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
        } else {
            reply(message.getChatId(), message.getMessageId(), systemArg);
        }
    }

    @Async
    @Scheduled(fixedDelay = 5L, timeUnit = TimeUnit.MINUTES)
    public void retrieveAndSendAchievements() {
        final PhilCommand command = commands.get(SystemCommandTypes.XBOX_ACHIEVEMENTS);
        final String result = command.execute(systemRequest(0L));

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
    public void sendMessage(Long chatId, String text) {
        final SendMessage sender = SendMessage.builder()
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

    private CommandRequest systemRequest(final Long chatId) {
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
