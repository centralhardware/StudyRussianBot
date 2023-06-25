CREATE TABLE IF NOT EXISTS study_russian
(
    date_time DateTime,
    chat_id BIGINT,
    username Nullable(String),
    first_name Nullable(String),
    last_name Nullable(String),
    is_premium bool,
    lang text,
    text VARCHAR(256),
    )
    engine = MergeTree
    ORDER BY date_time