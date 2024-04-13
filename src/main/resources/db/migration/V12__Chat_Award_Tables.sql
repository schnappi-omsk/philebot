CREATE TABLE chat_awards
(
    tg_id      VARCHAR(255) NOT NULL,
    award_date DATE,
    PRIMARY KEY (award_date)
);

CREATE TABLE award_nominee
(
    tg_id      VARCHAR(255) NOT NULL,
    tg_name    VARCHAR(255) NOT NULL,
    PRIMARY KEY (tg_id)
);

CREATE TABLE award_msg
(
    id      SERIAL PRIMARY KEY,
    message VARCHAR(255) NOT NULL
);

INSERT INTO award_msg (message) VALUES ('Поздравляю, %s, сегодня ты пидор дня!');
INSERT INTO award_msg (message) VALUES ('Великолепная новость: сегодня ты — главный! В голосовании за пидора дня. Поздравляем, %s!');
INSERT INTO award_msg (message) VALUES ('Сегодня твой день, герой! Герой пидорства, конечно. Поздравляем, %s');
INSERT INTO award_msg (message) VALUES ('Шаг вперед к славе! Сегодня — твой день! День быть пидором, конечно. Поздравляем, %s!');
INSERT INTO award_msg (message) VALUES ('Поздравляю, %s, сегодня ты пидор дня!');
INSERT INTO award_msg (message) VALUES ('Сегодняшний чемпион — %s! Первое место в номинации "Пидор дня"!');
INSERT INTO award_msg (message) VALUES ('Быть пидором дня — это вам не в крынку бздеть. Поздравляю, %s!');