package com.gundomrays.philebot.telegram.config;

import com.gundomrays.philebot.telegram.bot.PhilBot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Configuration
public class TelegramConfig {

    @Value("${tg.apiToken}")
    private String apiToken;

    @Bean
    public PhilBot philBot() {
        return new PhilBot(telegramClient(), "Phil E-Bot");
    }

    @Bean
    public TelegramClient telegramClient() {
        return new OkHttpTelegramClient(apiToken);
    }

}
