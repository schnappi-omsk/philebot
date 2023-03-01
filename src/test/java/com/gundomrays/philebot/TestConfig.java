package com.gundomrays.philebot;

import com.gundomrays.philebot.xbox.xapi.XApiClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.telegram.telegrambots.meta.TelegramBotsApi;

@TestConfiguration
public class TestConfig {

    @MockBean
    RedisConnectionFactory redisConnectionFactory;

    @MockBean
    XApiClient xApiClient;

    @MockBean
    TelegramBotsApi telegramBotsApi;

}
