# Migrations

Flyway migrations for the bot's Postgres database, in `postgres/`:

| Version | What |
|---|---|
| `V1__create_rules_words.sql` | `rules` and `words` tables |
| `V2__seed_rules_words.sql` | 22 rules and 1824 words |
| `V3__create_progress_tables.sql` | `checked_word`, `checked_wrong_word`, `checked_rule` |

Run them with the Flyway CLI:

```sh
docker run --rm -v "$PWD/migrations/postgres:/flyway/sql" flyway/flyway \
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

## Existing user progress

`migrate_redis_to_postgres.sh` copies the three per-chat sets out of the old
Redis into the tables `V3` creates. Run it after `migrate`, with the bot
stopped; it only reads Redis and is safe to re-run.

```sh
REDIS_URL=redis://HOST:6379 PG_URL=postgres://USER:PASSWORD@HOST:PORT/DATABASE \
  ./migrations/migrate_redis_to_postgres.sh
```

`DRY_RUN=1` prints what would be copied without writing. Note the Redis
instance is shared with another bot, so it should not be shut down once this
migration is done — only `REDIS_URL` goes away from this bot's environment.
