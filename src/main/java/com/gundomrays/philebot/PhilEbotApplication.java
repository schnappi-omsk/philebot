package com.gundomrays.philebot;

import com.gundomrays.philebot.telegram.bot.PhilBot;
import com.gundomrays.philebot.xbox.xapi.XBoxUserRegistrationService;
import com.gundomrays.philebot.xbox.xapi.XboxUserActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.telegram.telegrambots.meta.TelegramBotsApi;

@SpringBootApplication
public class PhilEbotApplication {

    @Autowired
    XBoxUserRegistrationService registrationService;

    @Autowired
    XboxUserActivityService activityService;

    final TelegramBotsApi telegramBotsApi;

    @Autowired
    PhilBot philBot;

    public PhilEbotApplication(TelegramBotsApi telegramBotsApi) {
        this.telegramBotsApi = telegramBotsApi;
    }

    public static void main(String[] args) {
        SpringApplication.run(PhilEbotApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            telegramBotsApi.registerBot(philBot);
            //registrationService.registerUser("schnappi0msk");
            //xApiClient.titleHistory("2535418020562953");
        };
    }

}
