#!/usr/bin/env bash
# Shared container helpers. Passwords are passed only through process environment.
set -euo pipefail

TRANSITOR_DUMP_SHA256=901e73a7214efa0bded9eda288d27e6d3d7d2b164ad302041af87ebc8886c074
TRANSITOR_DUMP_SIZE=1103158799
TRANSITOR_SCHEMA_VERSION=1
TRANSITOR_DUMP=/opt/transitor-data/dump-gtfs-202406251610.sql

root_sql() {
    MYSQL_PWD="${MARIADB_ROOT_PASSWORD:?Missing database root password}" \
        mariadb --no-defaults --protocol=socket --socket=/run/mysqld/mysqld.sock \
        --user=root --connect-timeout=2 --max-allowed-packet=128M --batch --skip-column-names "$@"
}

app_can_connect() {
    MYSQL_PWD="${MARIADB_PASSWORD:?Missing database password}" \
        mariadb --no-defaults --protocol=tcp --host=127.0.0.1 --port=3306 \
        --user=transitor --database=gtfs --connect-timeout=2 --batch --skip-column-names \
        --execute='SELECT 1' >/dev/null 2>&1
}

import_state() {
    local state_table
    state_table=$(root_sql --execute="SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='transitor_setup' AND table_name='import_state'")
    if [[ "$state_table" == 0 ]]; then
        return 0
    fi
    root_sql --execute="SELECT CONCAT(schema_version, ':', dump_sha256, ':', status) FROM transitor_setup.import_state WHERE id=1"
}

application_schema_is_ready() {
    local result
    result=$(root_sql --database=gtfs --execute="
        SELECT
          (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='gtfs'
            AND table_name IN ('points','single_bus_rides','walking_ages','stops_maastricht','trips_maastricht','shapes_maastricht','routes')) = 7
          AND (SELECT COUNT(*) FROM information_schema.views WHERE table_schema='gtfs'
            AND table_name IN ('points','single_bus_rides','walking_ages')) = 3
          AND EXISTS(SELECT 1 FROM points LIMIT 1)
          AND EXISTS(SELECT 1 FROM single_bus_rides LIMIT 1)
          AND EXISTS(SELECT 1 FROM walking_ages LIMIT 1)
          AND EXISTS(SELECT 1 FROM stops_maastricht LIMIT 1)
          AND EXISTS(SELECT 1 FROM trips_maastricht LIMIT 1)
          AND EXISTS(SELECT 1 FROM shapes_maastricht LIMIT 1)
          AND EXISTS(SELECT 1 FROM routes LIMIT 1);") || return 1
    [[ "$result" == 1 ]]
}
