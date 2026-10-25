#!/usr/bin/env bash
# Integration checks for importer failures and data preservation. Requires Docker
# Compose; creates and removes only its own temporary project and volume.
set -euo pipefail
umask 077
repo=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)
source "$repo/docker/common.sh"
mkdir -p "$repo/.transitor"
test_dir=$(mktemp -d "$repo/.transitor/import-guards.XXXXXX")
project="transitor-import-guards-$(date +%s)-$$"
export TRANSITOR_TEST_DATA_DIR="$test_dir/data"
mkdir -p "$TRANSITOR_TEST_DATA_DIR"
root_password=$(od -An -N32 -tx1 /dev/urandom | tr -d ' \n')
app_password=$(od -An -N32 -tx1 /dev/urandom | tr -d ' \n')
printf 'TRANSITOR_DOCKER_ROOT_PASSWORD=%s\nTRANSITOR_DOCKER_PASSWORD=%s\n' \
    "$root_password" "$app_password" > "$test_dir/docker.env"
unset root_password app_password
cat > "$test_dir/override.yaml" <<'YAML'
services:
  db:
    entrypoint: ["/usr/local/bin/docker-entrypoint.sh"]
    healthcheck:
      disable: true
    volumes:
      - type: bind
        source: ${TRANSITOR_TEST_DATA_DIR}
        target: /opt/transitor-data
        read_only: true
YAML
compose=(docker compose --project-name "$project" --env-file "$test_dir/docker.env" \
    -f "$repo/compose.yaml" -f "$test_dir/override.yaml")
cleanup() {
    result=$?
    trap - EXIT
    "${compose[@]}" down --volumes --remove-orphans > "$test_dir/cleanup.log" 2>&1 || result=1
    if [[ "$result" == 0 ]]; then
        rm -rf -- "$test_dir"
    else
        rm -f -- "$test_dir/docker.env"
        echo "Importer check logs: $test_dir" >&2
    fi
    exit "$result"
}
trap cleanup EXIT
"${compose[@]}" up -d > "$test_dir/start.log" 2>&1
ready=false
for attempt in $(seq 1 90); do
    if "${compose[@]}" exec -T db bash -c 'source /opt/transitor/common.sh; app_can_connect' >/dev/null 2>&1; then
        ready=true
        break
    fi
    sleep 1
done
[[ "$ready" == true ]] || { echo 'Test database did not start.' >&2; exit 1; }

sql() {
    "${compose[@]}" exec -T db bash -c 'source /opt/transitor/common.sh; root_sql --execute "$1"' _ "$1"
}
expect_exit() {
    local expected=$1 actual=0
    if "${compose[@]}" exec -T db bash /opt/transitor/import-gtfs.sh > "$test_dir/import.log" 2>&1; then
        actual=0
    else
        actual=$?
    fi
    if [[ "$actual" != "$expected" ]]; then
        cat "$test_dir/import.log" >&2
        echo "Expected importer exit $expected, got $actual." >&2
        exit 1
    fi
}
assert_value() {
    local actual
    actual=$(sql "$1")
    [[ "$actual" == "$2" ]] || { echo "Unexpected database result: $actual (expected $2)." >&2; exit 1; }
}
write_pointer() {
    printf 'version https://git-lfs.github.com/spec/v1\noid sha256:%s\nsize %s\n' \
        "$TRANSITOR_DUMP_SHA256" "$TRANSITOR_DUMP_SIZE" > "$TRANSITOR_TEST_DATA_DIR/dump-gtfs-202406251610.sql"
}

expect_exit 42
write_pointer
expect_exit 42
printf 'incomplete download\n' > "$TRANSITOR_TEST_DATA_DIR/dump-gtfs-202406251610.sql"
expect_exit 43
assert_value "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema IN ('gtfs','transitor_setup')" 0
write_pointer

sql 'CREATE TABLE gtfs.preservation_sentinel (id INT PRIMARY KEY); INSERT INTO gtfs.preservation_sentinel VALUES (7)' >/dev/null
expect_exit 44
assert_value "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name='transitor_setup'" 0

sql "CREATE DATABASE transitor_setup; CREATE TABLE transitor_setup.import_state (id TINYINT PRIMARY KEY, schema_version INT NOT NULL, dump_sha256 CHAR(64) NOT NULL, status VARCHAR(16) NOT NULL, completed_at TIMESTAMP NULL DEFAULT NULL); INSERT INTO transitor_setup.import_state VALUES (1, $TRANSITOR_SCHEMA_VERSION, '$TRANSITOR_DUMP_SHA256', 'importing', NULL)" >/dev/null
expect_exit 42
assert_value 'SELECT id FROM gtfs.preservation_sentinel' 7
assert_value 'SELECT status FROM transitor_setup.import_state WHERE id=1' importing

sql "UPDATE transitor_setup.import_state SET status='complete', completed_at='2024-01-01 00:00:00' WHERE id=1" >/dev/null
expect_exit 44
assert_value 'SELECT id FROM gtfs.preservation_sentinel' 7
assert_value 'SELECT status FROM transitor_setup.import_state WHERE id=1' complete

sql 'UPDATE transitor_setup.import_state SET schema_version=2 WHERE id=1' >/dev/null
expect_exit 44
assert_value 'SELECT schema_version FROM transitor_setup.import_state WHERE id=1' 2
assert_value 'SELECT id FROM gtfs.preservation_sentinel' 7

echo 'Passed 7 importer guard scenarios: missing/pointer/invalid dumps and preservation of unowned, partial, damaged, and mismatched databases.'
