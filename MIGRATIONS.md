# Migrations

The bot runs Flyway itself: `migrate()` in `Main.kt` executes before long
polling starts, using the same `POSTGRES_URL` / `POSTGRES_USERNAME` /
`POSTGRES_PASSWORD` the rest of the app uses. Deploying a new build applies
whatever is pending; nothing has to be run by hand.

The migrations live on the classpath, in `src/main/resources/db/migration`:

| Version | What |
|---|---|
| `V1__create_rules_words.sql` | `rules` and `words` tables |
| `V2__seed_rules_words.sql` | 22 rules and 1824 words |
| `V3__create_progress_tables.sql` | `checked_word`, `checked_wrong_word`, `checked_rule` |
| `V4__seed_progress.sql` | 23 818 rows of per-user progress carried over from Redis |

To apply them without starting the bot:

```sh
docker run --rm -v "$PWD/src/main/resources/db/migration:/flyway/sql" flyway/flyway \
  -url=jdbc:postgresql://HOST:PORT/DATABASE -user=USER -password=PASSWORD migrate
```

## Where the seed data comes from

`V2` was generated from `src/main/resources/rule.json` and `word.json`, the
resources the bot shipped with until commit `d025a2b` ("switch to postgres")
moved rules and words into the database — recover them from `d025a2b^` if the
seed ever has to be rebuilt from scratch.

Two entries in `word.json` named a section no rule declares: `r17м` is a
Cyrillic-м typo for `r17` and was remapped, while the single `15` entry also
had a broken `answer` field and was dropped — 1825 words in, 1824 in `V2`.

`V4` is the per-user progress as it stood in the old Redis instance: 17 951
right words, 5 818 wrong ones and 49 completed rules across 56 chats. Every row
is `ON CONFLICT DO NOTHING`, so re-applying it changes nothing.

That Redis instance is shared with another bot and must stay up — only
`REDIS_URL` goes away from this bot's environment.
