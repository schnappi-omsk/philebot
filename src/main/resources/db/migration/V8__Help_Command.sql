CREATE TABLE help
(
    command_id     VARCHAR(255) PRIMARY KEY,
    command_manual TEXT
);

INSERT INTO help (command_id, command_manual)
VALUES ('/reg', 'Формат: /reg [Xbox Gamertag]. Регистрация в боте');
INSERT INTO help (command_id, command_manual)
VALUES ('/find', 'Формат: /find [Название/фрагмент названия игры]. Поиск игры по базе бота.');
INSERT INTO help (command_id, command_manual)
VALUES ('/last', 'Формат: /last. Вывести топ по игре, ачивка из которой была зарегистрирована в боте последней.');
INSERT INTO help (command_id, command_manual)
VALUES ('/top', 'Формат: /top. Топ бота по рейтингу Xbox. Может отличаться от данных в приложениях Xbox в меньшую сторону.');
INSERT INTO help (command_id, command_manual)
VALUES ('/ping', 'Формат: /ping [on/off]. Если включено (on), то при каждой ачивке пользователя будет тэгать, если выключено -- нет.');