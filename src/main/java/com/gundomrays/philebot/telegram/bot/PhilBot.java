package com.gundomrays.philebot.telegram.bot;

import com.gundomrays.philebot.command.CommandRequest;
import com.gundomrays.philebot.command.PhilCommand;
import com.gundomrays.philebot.telegram.exception.TelegramException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Map;

@EnableAsync
public class PhilBot extends TelegramLongPollingBot {

    private static final Logger log = LoggerFactory.getLogger(PhilBot.class);

    @Value("${tg.botName}")
    private String botName;

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
        log.info("Message from: {}, text: {}", from.getUserName(), messageText);

        if (message.isCommand()) {
            CommandRequest request = parseCommand(messageText);
            log.info("Command {} was received", request.getCommand());
            PhilCommand command = commands.get(request.getCommand());
            if (command != null) {
                final String result = command.execute(from.getUserName(), request.getArgument());
                reply(from.getId(), message.getMessageId(), result);
            } else {
                log.info("Command not found: {}", messageText);
                reply(from.getId(), message.getMessageId(), "Command not found: " + messageText);
            }
        } else {
            reply(from.getId(), message.getMessageId(), "Test!");
        }
    }

    public void reply(Long idTo, Integer msgId, String text) {
        final SendMessage sender = SendMessage.builder()
                .chatId(idTo)
                .replyToMessageId(msgId)
                .text(text)
                .build();
        try {
            execute(sender);
        } catch (TelegramApiException e) {
            throw new TelegramException(e.getMessage(), e);
        }
    }
    public void sendMessage(Long idTo, String text) {
        final SendMessage sender = SendMessage.builder()
                .chatId(idTo)
                .text(text)
                .build();
        try {
            execute(sender);
        } catch (TelegramApiException e) {
            throw new TelegramException(e.getMessage(), e);
        }
    }


    private CommandRequest parseCommand(String msg) {
        final CommandRequest request = new CommandRequest();
        int spaceIndex = msg.indexOf(" ");
        final String command = spaceIndex > -1 ? msg.substring(0, spaceIndex) : msg;
        request.setCommand(command);
        if (spaceIndex > -1) {
            request.setArgument(msg.substring(spaceIndex).trim());
        }
        return request;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

}
