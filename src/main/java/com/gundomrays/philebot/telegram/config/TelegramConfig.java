package com.gundomrays.philebot.telegram.config;

import com.gundomrays.philebot.telegram.bot.PhilBot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.Map;

@Configuration
public class TelegramConfig {

    @Value("${tg.apiToken}")
    private String apiToken;

    @Bean
    public PhilBot philBot() {
        return new PhilBot(apiToken);
    }

    @Bean
    public TelegramBotsApi telegramBotsApi() throws TelegramApiException {
        return new TelegramBotsApi(DefaultBotSession.class);
    }

}
