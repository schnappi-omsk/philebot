CREATE TABLE settings
(
    id          VARCHAR(255) PRIMARY KEY,
    description TEXT,
    value       VARCHAR(255)
);

-- Initialize settings table witn an empty chat id.
INSERT INTO settings (id, description, value)
VALUES ('CHAT_ID', 'Identifier of the chat where bot is added', NULL);