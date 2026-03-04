package com.gundomrays.philebot.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;

import java.io.InputStream;

@Service
public class ChatGptClient {

    private final static Logger log = LoggerFactory.getLogger(ChatGptClient.class);

    private final ChatClient chatClient;

    public ChatGptClient(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String extractTextFromImage(final InputStream inputStream, final String contentType) {
        final String aiResult = chatClient.prompt()
                .options(OpenAiChatOptions.builder().model(OpenAiApi.ChatModel.GPT_4_O).build())
                .user(usrMsg -> usrMsg
                        .text("Extract text from provided image as is.")
                        .text("Text could be in Russian, Ukrainian or English.")
                        .text("Return plain text only.")
                        .media(MimeType.valueOf(contentType), new InputStreamResource(inputStream)))
                .call()
                .content();
        log.info("AI result for file: {}", aiResult);
        return aiResult;
    }

}
