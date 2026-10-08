package com.gundomrays.philebot.psn;

import com.gundomrays.philebot.data.PsnTrophyDataService;
import com.gundomrays.philebot.messaging.MessageQueue;
import com.gundomrays.philebot.psn.api.PsnApiClient;
import com.gundomrays.philebot.psn.auth.PsnAuthService;
import com.gundomrays.philebot.telegram.data.SettingsRepository;
import com.gundomrays.philebot.worker.PhilTrophyRetriever;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.http.codec.CodecsAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

// Checks that the PSN beans can be created from configuration, without a database or network
public class PsnWiringTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class, CodecsAutoConfiguration.class,
                    WebClientAutoConfiguration.class))
            .withBean(SettingsRepository.class, () -> Mockito.mock(SettingsRepository.class))
            .withBean(PsnTrophyDataService.class, () -> Mockito.mock(PsnTrophyDataService.class))
            .withBean(MessageQueue.class)
            .withUserConfiguration(PsnAuthService.class, PsnApiClient.class, PsnTrophyQueue.class,
                    PsnTrophyActivityService.class, PsnUserRegistrationService.class, PhilTrophyRetriever.class)
            .withPropertyValues(
                    "psn.npsso=", "psn.clientId=test-client", "psn.clientBasicAuth=dGVzdA==",
                    "psn.redirectUri=com.example.test://redirect", "psn.scope=test-scope",
                    "psn.authUrl=https://localhost/auth", "psn.apiUrl=https://localhost/api",
                    "psn.legacyProfileUrl=https://localhost/profile", "psn.userAgent=test-agent",
                    "psn.language=en-US", "psn.requestsPerMin=20.0", "psn.titlesLimit=50", "psn.limitPerUser=5",
                    "psn.minTrophyType=gold", "psn.refreshWarningDays=7",
                    "messages.psn.tokenExpiring=expires %s", "messages.psn.authFailed=failed",
                    "ebot.serviceHost=https://bot.example.com");

    @Test
    public void testPsnBeansAreCreated() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(PsnAuthService.class);
            assertThat(context).hasSingleBean(PsnApiClient.class);
            assertThat(context).hasSingleBean(PsnTrophyActivityService.class);
            assertThat(context).hasSingleBean(PsnUserRegistrationService.class);
            assertThat(context).hasSingleBean(PhilTrophyRetriever.class);
        });
    }
}
