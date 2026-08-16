#!/usr/bin/env bash
#
# One-shot copy of user progress from the old Redis store into the Postgres
# tables used by Progress.kt. Redis is only read, and every row is inserted
# with ON CONFLICT DO NOTHING, so the script is safe to re-run.
#
# Requires redis-cli and psql. Apply migration/schema.sql first, and run this
# while the bot is stopped.
#
#   REDIS_URL=redis://10.168.0.77:6379 \
#   PG_URL=postgres://user:pass@host:5432/znatoki_rus \
#   ./migration/migrate_redis_to_postgres.sh
#
# DRY_RUN=1 dumps what would be copied and touches nothing.
#
# Redis key layout being read (each key is a set of word / rule names):
#   <chatId>_checked_word         -> checked_word (word)
#   <chatId>_checked_wrong_word   -> checked_wrong_word (word)
#   <chatId>_checked_rule         -> checked_rule (rule)
#
set -euo pipefail

: "${REDIS_URL:?set REDIS_URL, e.g. redis://host:6379}"
: "${PG_URL:?set PG_URL, e.g. postgres://user:pass@host:5432/znatoki_rus}"

DRY_RUN="${DRY_RUN:-0}"
tmp="$(mktemp -d)"
trap 'rm -rf "$tmp"' EXIT

# Dump every set whose key ends in $suffix as TSV "<chat_id>\t<member>".
dump() {
    local suffix="$1" out="$2"
    # --scan iterates instead of blocking the server the way KEYS does.
    redis-cli -u "$REDIS_URL" --scan --pattern "*${suffix}" |
        while read -r key; do
            chat_id="${key%"$suffix"}"
            # An old markRuleAsComplete bug wrote a stray space into the key.
            chat_id="${chat_id//[[:space:]]/}"
            if [[ ! "$chat_id" =~ ^-?[0-9]+$ ]]; then
                echo "skipping unexpected key: $key" >&2
                continue
            fi
            redis-cli -u "$REDIS_URL" smembers "$key" |
                while IFS= read -r member; do
                    [[ -n "$member" ]] || continue
                    # TSV for COPY: escape backslash and tab, drop CR.
                    member="${member//\\/\\\\}"
                    member="${member//$'\t'/\\t}"
                    member="${member//$'\r'/}"
                    printf '%s\t%s\n' "$chat_id" "$member"
                done
        done >"$out"
    echo "$(wc -l <"$out") rows from *${suffix}" >&2
}

dump "_checked_word" "$tmp/checked_word.tsv"
dump "_checked_wrong_word" "$tmp/checked_wrong_word.tsv"
dump "_checked_rule" "$tmp/checked_rule.tsv"

if [[ "$DRY_RUN" == "1" ]]; then
    echo "DRY_RUN=1 — nothing written to Postgres. Dumps left in $tmp:" >&2
    wc -l "$tmp"/*.tsv >&2
    trap - EXIT
    exit 0
fi

load() {
    local table="$1" column="$2" file="$3"
    psql "$PG_URL" -v ON_ERROR_STOP=1 <<SQL
BEGIN;
CREATE TEMP TABLE staging (chat_id BIGINT, $column TEXT) ON COMMIT DROP;
\\copy staging FROM '$file'
INSERT INTO $table (chat_id, $column)
SELECT chat_id, $column FROM staging
ON CONFLICT DO NOTHING;
COMMIT;
SQL
    echo "loaded $table" >&2
}

load checked_word word "$tmp/checked_word.tsv"
load checked_wrong_word word "$tmp/checked_wrong_word.tsv"
load checked_rule rule "$tmp/checked_rule.tsv"

psql "$PG_URL" -c "SELECT 'checked_word' AS table, count(*) FROM checked_word
                   UNION ALL SELECT 'checked_wrong_word', count(*) FROM checked_wrong_word
                   UNION ALL SELECT 'checked_rule', count(*) FROM checked_rule;"
