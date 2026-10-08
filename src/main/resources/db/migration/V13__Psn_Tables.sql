-- PSN refresh tokens are stored in settings and may be longer than 255 characters
ALTER TABLE settings ALTER COLUMN value SET DATA TYPE TEXT;

-- PSN profile, linked to a Telegram user
CREATE TABLE psn_profile
(
    account_id    VARCHAR(255) PRIMARY KEY,
    online_id     VARCHAR(255)             NOT NULL,
    tg_id         BIGINT                   NOT NULL,
    tg_username   VARCHAR(255)             NOT NULL,
    ping          BOOLEAN                  NOT NULL DEFAULT FALSE,
    active        BOOLEAN                  NOT NULL DEFAULT TRUE,
    registered_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_psn_tg_username ON psn_profile (tg_username);

-- Earned trophy counts per title, used to detect changes
CREATE TABLE psn_title_progress
(
    account_id          VARCHAR(255) NOT NULL,
    np_communication_id VARCHAR(64)  NOT NULL,
    np_service_name     VARCHAR(32)  NOT NULL,
    title_name          VARCHAR(255),
    title_icon_url      TEXT,
    platform            VARCHAR(64),
    earned_bronze       INT          NOT NULL DEFAULT 0,
    earned_silver       INT          NOT NULL DEFAULT 0,
    earned_gold         INT          NOT NULL DEFAULT 0,
    earned_platinum     INT          NOT NULL DEFAULT 0,
    progress            INT,
    last_updated        TIMESTAMP WITH TIME ZONE,
    trophies_tracked    BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (account_id, np_communication_id),
    FOREIGN KEY (account_id) REFERENCES psn_profile (account_id)
);

-- Earned trophies already known to the bot, used for deduplication
CREATE TABLE psn_earned_trophy
(
    account_id          VARCHAR(255) NOT NULL,
    np_communication_id VARCHAR(64)  NOT NULL,
    trophy_id           INT          NOT NULL,
    trophy_type         VARCHAR(16),
    earned_at           TIMESTAMP WITH TIME ZONE,
    PRIMARY KEY (account_id, np_communication_id, trophy_id),
    FOREIGN KEY (account_id) REFERENCES psn_profile (account_id)
);

INSERT INTO help (command_id, command_manual)
VALUES ('/psnreg', 'Формат: /psnreg [PSN Online ID]. Регистрация PSN-аккаунта в боте. Трофеи должны быть видны аккаунту бота (приватность "Все" или бот в друзьях).');
