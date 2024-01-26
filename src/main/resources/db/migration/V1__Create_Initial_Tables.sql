--Profile table
CREATE TABLE profile
(
    xuid             VARCHAR(255) PRIMARY KEY,
    gamertag         VARCHAR(255),
    tg_id            VARCHAR(255) NOT NULL,
    tg_username      VARCHAR(255),
    last_achievement TIMESTAMP WITHOUT TIME ZONE DEFAULT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_gamertag ON profile (gamertag);
CREATE UNIQUE INDEX IF NOT EXISTS idx_tg_username ON profile (tg_username);

-- Title Table
CREATE TABLE title
(
    title_id VARCHAR(255) PRIMARY KEY,
    name     VARCHAR(255)
);
CREATE INDEX IF NOT EXISTS idx_title_id ON title (title_id);
CREATE INDEX IF NOT EXISTS idx_name ON title (name);

-- TitleHistory Table
CREATE TABLE title_history
(
    title_history_id SERIAL PRIMARY KEY,
    xuid             VARCHAR(255),
    title_id         VARCHAR(255),
    FOREIGN KEY (xuid) references profile (xuid),
    FOREIGN KEY (title_id) references title (title_id)
);

--Activities
CREATE TABLE activity
(
    activity_id SERIAL PRIMARY KEY,
    xuid        VARCHAR(255) NOT NULL
);

-- Activity items
CREATE TABLE activity_item
(
    user_xuid               VARCHAR(255),
    activity_id             INT,
    content_title           VARCHAR(255),
    title_id                VARCHAR(255),
    achievement_name        VARCHAR(255),
    achievement_description VARCHAR(255),
    achievement_icon        VARCHAR(255),
    gamerscore              INT,
    rarity_percentage       INT,
    date                    TIMESTAMP NOT NULL,
    PRIMARY KEY (user_xuid, activity_id),
    FOREIGN KEY (activity_id) REFERENCES activity (activity_id)
);
CREATE INDEX IF NOT EXISTS idx_user_xuid ON activity_item (user_xuid);
CREATE INDEX IF NOT EXISTS idx_content_title ON activity_item (content_title);
CREATE INDEX IF NOT EXISTS idx_title_id ON activity_item (title_id);
