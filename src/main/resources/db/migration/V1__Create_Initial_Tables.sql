--Profile table
CREATE TABLE profile (
                         id SERIAL PRIMARY KEY,
                         gamertag VARCHAR(255),
                         tg_username VARCHAR(255)
);
CREATE UNIQUE INDEX idx_gamertag ON profile (gamertag);
CREATE UNIQUE INDEX idx_tg_username ON profile (tg_username);

-- Activity items
CREATE TABLE activity_item (
                               user_xuid VARCHAR(255),
                               activity_id INT,
                               content_title VARCHAR(255),
                               title_id VARCHAR(255),
                               achievement_name VARCHAR(255),
                               achievement_description VARCHAR(255),
                               achievement_icon VARCHAR(255),
                               gamerscore INT,
                               rarity_percentage INT,
                               date TIMESTAMP NOT NULL,
                               PRIMARY KEY(user_xuid, activity_id),
                               FOREIGN KEY(activity_id) REFERENCES activity(id)
);
CREATE INDEX idx_user_xuid ON activity_item (user_xuid);
CREATE INDEX idx_content_title ON activity_item (content_title);
CREATE INDEX idx_title_id ON activity_item (title_id);

--Activities
CREATE TABLE activity (
                          id SERIAL PRIMARY KEY
);

-- TitleProgress Table
CREATE TABLE title_progress (
                                id SERIAL PRIMARY KEY,
                                progress_percentage INT
);

-- Title Table
CREATE TABLE title (
                       id SERIAL PRIMARY KEY,
                       title_id VARCHAR(255),
                       name VARCHAR(255),
                       progress_id INT,
                       FOREIGN KEY (progress_id) REFERENCES title_progress(id)
);
CREATE INDEX idx_title_id ON title (title_id);
CREATE INDEX idx_name ON title (name);

-- TitleHistory Table
CREATE TABLE title_history (
                               id SERIAL PRIMARY KEY,
                               xuid VARCHAR(255)
);

-- As the relationship between Title and TitleHistory is one-to-many,
-- a separate table is created to represent this relationship
CREATE TABLE title_history_title (
                                     title_history_id INT,
                                     title_id INT,
                                     FOREIGN KEY (title_history_id) REFERENCES title_history(id),
                                     FOREIGN KEY (title_id) REFERENCES title(id)
);
