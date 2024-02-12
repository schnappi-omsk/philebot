package com.gundomrays.philebot.telegram.config;

import com.gundomrays.philebot.telegram.data.SettingsRepository;
import com.gundomrays.philebot.telegram.domain.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SettingsService {

    private static final Logger log = LoggerFactory.getLogger(SettingsService.class);

    private static final String CHAT_ID = "CHAT_ID";

    private final SettingsRepository settingsRepository;

    public SettingsService(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public Long chatId() {
        return chatId(null);
    }

    public Long chatId(final String newChatId) {
        Settings chatIdSetting = settingsRepository.findById(CHAT_ID).orElse(null);

        if (chatIdSetting == null) {
            log.warn("{} is not present, something wrong with settings!", CHAT_ID);
            chatIdSetting = new Settings();
            chatIdSetting.setId(CHAT_ID);
        }

        final String currentChatId = chatIdSetting.getValue();

        if ((newChatId != null && !newChatId.isEmpty()) && (currentChatId == null || currentChatId.isEmpty())) {
            log.info("Initializing bot for chat={}", newChatId);
            chatIdSetting.setValue(newChatId);
            settingsRepository.save(chatIdSetting);
        } else {
            log.warn("Cannot update chatId. Bot is already in chat {}", currentChatId);
        }

        return chatIdSetting.getValue() == null || chatIdSetting.getValue().isEmpty()
                ? null
                : Long.parseLong(chatIdSetting.getValue());
    }

}
