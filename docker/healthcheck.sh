#!/usr/bin/env bash
set -euo pipefail
source /opt/transitor/common.sh

[[ "$(import_state)" == "${TRANSITOR_SCHEMA_VERSION}:${TRANSITOR_DUMP_SHA256}:complete" ]]
app_can_connect
application_schema_is_ready
