package com.gundomrays.philebot;

import com.gundomrays.philebot.telegram.bot.PhilBot;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.telegram.telegrambots.meta.TelegramBotsApi;

@SpringBootApplication(scanBasePackages = {"com.gundomrays.philebot"})
public class PhilEbotApplication {
    final TelegramBotsApi telegramBotsApi;

    final
    PhilBot philBot;

    public PhilEbotApplication(TelegramBotsApi telegramBotsApi, PhilBot philBot) {
        this.telegramBotsApi = telegramBotsApi;
        this.philBot = philBot;
    }

    public static void main(String[] args) {
        SpringApplication.run(PhilEbotApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            telegramBotsApi.registerBot(philBot);
        };
    }

}
