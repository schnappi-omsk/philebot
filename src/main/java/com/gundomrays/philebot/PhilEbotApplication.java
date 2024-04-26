package com.gundomrays.philebot;

import com.gundomrays.philebot.telegram.bot.PhilBot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

@SpringBootApplication(scanBasePackages = {"com.gundomrays.philebot"})
public class PhilEbotApplication {
    final TelegramBotsLongPollingApplication botsApplication;

    final PhilBot philBot;

    @Value("${tg.apiToken}")
    private String apiToken;

    public PhilEbotApplication(PhilBot philBot) {
        this.botsApplication = new TelegramBotsLongPollingApplication();
        this.philBot = philBot;
    }

    public static void main(String[] args) {
        SpringApplication.run(PhilEbotApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> botsApplication.registerBot(apiToken, philBot);
    }

}
