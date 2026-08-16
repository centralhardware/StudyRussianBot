-- Tables backing Progress.kt. Apply once, before starting the new build:
--   psql "$PG_URL" -v ON_ERROR_STOP=1 -f migration/schema.sql
--
-- Each table is a set: one row per (user, word/rule), no duplicates.

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
