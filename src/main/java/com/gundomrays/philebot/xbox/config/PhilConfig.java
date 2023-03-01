package com.gundomrays.philebot.xbox.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class PhilConfig {

    @Value("${xapi.baseUrl}")
    private String baseUrl;

    @Value("${xapi.apiToken}")
    private String apiToken;

    @Bean
    public WebClient xapiClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeaders(httpHeaders -> {
                    httpHeaders.add("Authorization", apiToken);
                    httpHeaders.add("Accept-Language", "en-US");
                })
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(5000 * 1024))
                .build();
    }

}
