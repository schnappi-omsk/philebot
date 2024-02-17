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

        if (chatIdSetting != null && chatIdSetting.isSealed()) {
            log.info("Cannot update chatId, setting is sealed.");
            return chatIdValueFromSettings(chatIdSetting);
        }

        if (chatIdSetting == null) {
            log.warn("{} is not present, something wrong with settings!", CHAT_ID);
            chatIdSetting = new Settings();
            chatIdSetting.setId(CHAT_ID);
        }

        final String currentChatId = chatIdSetting.getValue();

        boolean validChatIds = (newChatId != null && !newChatId.isEmpty())
                && (currentChatId == null || currentChatId.isEmpty());
        if (validChatIds) {
            log.info("Initializing bot for chat={}", newChatId);
            chatIdSetting.setValue(newChatId);
            chatIdSetting.setSealed(true);
            settingsRepository.save(chatIdSetting);
        } else {
            log.warn("Cannot update chatId. Bot is already in chat {}", currentChatId);
        }

        return chatIdValueFromSettings(chatIdSetting);
    }

    private Long chatIdValueFromSettings(Settings chatIdSetting) {
        return chatIdSetting.getValue() == null || chatIdSetting.getValue().isEmpty()
                ? 0L
                : Long.parseLong(chatIdSetting.getValue());
    }

}
