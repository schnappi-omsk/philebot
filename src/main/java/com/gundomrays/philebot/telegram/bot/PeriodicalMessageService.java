package com.gundomrays.philebot.telegram.bot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PeriodicalMessageService {

    @Value("${messages.periodical}")
    private String message;

    public String message() {
        return message;
    }

}
