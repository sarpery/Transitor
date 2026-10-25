#!/usr/bin/env bash
# Runs after the official MariaDB entrypoint finishes its own initialization.
set -euo pipefail
source /opt/transitor/common.sh

state=$(import_state)
expected="${TRANSITOR_SCHEMA_VERSION}:${TRANSITOR_DUMP_SHA256}"
case "$state" in
    "$expected:complete")
        if ! application_schema_is_ready; then
            echo 'The completed transit database is damaged or incomplete; preserving it for review.' >&2
            exit 44
        fi
        echo 'The transit database is already imported; preserving existing local data.'
        exit 0
        ;;
    "$expected:importing")
        echo 'Resuming an interrupted transit database import.'
        ;;
    '')
        tables=$(root_sql --execute="SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='gtfs'")
        if [[ "$tables" != 0 ]]; then
            echo 'Existing transit data has no matching import record; refusing to overwrite it.' >&2
            exit 44
        fi
        ;;
    *)
        echo 'The database import record does not match this version; preserving existing data.' >&2
        exit 44
        ;;
esac

# A completed volume never needs the source dump again. Exit 42 tells the
# desktop launcher that an uninitialized volume still needs Git LFS data.
if [[ ! -f "$TRANSITOR_DUMP" ]]; then
    echo 'The transit dump is missing; download its Git LFS data and start again.' >&2
    exit 42
fi
if head -c 128 "$TRANSITOR_DUMP" | grep -q '^version https://git-lfs.github.com/spec/v1'; then
    echo 'The transit dump is a Git LFS pointer; download its data and start again.' >&2
    exit 42
fi
if [[ "$(stat --format=%s "$TRANSITOR_DUMP")" != "$TRANSITOR_DUMP_SIZE" ]]; then
    echo 'The transit dump size does not match the repository; no import was started.' >&2
    exit 43
fi
if ! printf '%s  %s\n' "$TRANSITOR_DUMP_SHA256" "$TRANSITOR_DUMP" | sha256sum --check --status; then
    echo 'The transit dump checksum does not match the repository; no import was started.' >&2
    exit 43
fi

echo 'Transit dump verified. Importing the historical timetable; this can take several minutes.'
root_sql --execute="
    CREATE DATABASE IF NOT EXISTS transitor_setup;
    CREATE TABLE IF NOT EXISTS transitor_setup.import_state (
        id TINYINT PRIMARY KEY,
        schema_version INT NOT NULL,
        dump_sha256 CHAR(64) NOT NULL,
        status VARCHAR(16) NOT NULL,
        completed_at TIMESTAMP NULL DEFAULT NULL
    );
    INSERT INTO transitor_setup.import_state (id, schema_version, dump_sha256, status)
      VALUES (1, $TRANSITOR_SCHEMA_VERSION, '$TRANSITOR_DUMP_SHA256', 'importing')
      ON DUPLICATE KEY UPDATE status='importing', completed_at=NULL;"

# The original dump contains its own DROP/CREATE statements. Only a fresh or
# matching, explicitly recorded incomplete import reaches this operation.
root_sql --database=gtfs < "$TRANSITOR_DUMP"
if ! application_schema_is_ready; then
    echo 'Import ended without the required nonempty transit tables and views; it is not ready.' >&2
    exit 1
fi
root_sql --execute="UPDATE transitor_setup.import_state SET status='complete', completed_at=CURRENT_TIMESTAMP WHERE id=1 AND schema_version=$TRANSITOR_SCHEMA_VERSION AND dump_sha256='$TRANSITOR_DUMP_SHA256' AND status='importing'"
echo 'Transit database import completed and its routing tables and views are ready.'
