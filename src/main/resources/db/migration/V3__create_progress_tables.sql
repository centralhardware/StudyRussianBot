-- Per-user progress, previously three Redis sets per chat. Each table is a
-- set: one row per (user, word/rule), duplicates dropped by the primary key.

CREATE TABLE IF NOT EXISTS checked_word (
    chat_id BIGINT NOT NULL,
    word    TEXT   NOT NULL,
    PRIMARY KEY (chat_id, word)
);

CREATE TABLE IF NOT EXISTS checked_wrong_word (
    chat_id BIGINT NOT NULL,
    word    TEXT   NOT NULL,
    PRIMARY KEY (chat_id, word)
);

CREATE TABLE IF NOT EXISTS checked_rule (
    chat_id BIGINT NOT NULL,
    rule    TEXT   NOT NULL,
    PRIMARY KEY (chat_id, rule)
);
