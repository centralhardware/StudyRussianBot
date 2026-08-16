-- Rules and words the bot tests against. The columns are the ones
-- WordMapper.kt and ruleMapper/wordMapper read.
--
-- parent = -1 means "top level": ruleMapper looks the parent up by id and gets
-- null back for -1.

CREATE TABLE IF NOT EXISTS rules (
    id     INTEGER PRIMARY KEY,
    name   TEXT    NOT NULL,
    parent INTEGER NOT NULL DEFAULT -1
);

CREATE TABLE IF NOT EXISTS words (
    word       TEXT    NOT NULL,
    right_word TEXT    NOT NULL,
    answer     TEXT    NOT NULL,
    ruleid     INTEGER NOT NULL REFERENCES rules (id)
);

CREATE INDEX IF NOT EXISTS words_ruleid_idx ON words (ruleid);
