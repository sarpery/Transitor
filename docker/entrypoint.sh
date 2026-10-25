#!/usr/bin/env bash
# Keep MariaDB and import readiness in one container lifecycle. In contrast to
# initdb.d alone, this resumes a recorded partial import on the next startup.
set -euo pipefail
source /opt/transitor/common.sh

server_pid=
import_pid=
stop_children() {
    trap - TERM INT
    if [[ -n "$server_pid" ]]; then
        kill -TERM "$server_pid" 2>/dev/null || true
    fi
    if [[ -n "$import_pid" ]]; then
        kill -TERM "$import_pid" 2>/dev/null || true
        wait "$import_pid" 2>/dev/null || true
    fi
    if [[ -n "$server_pid" ]]; then
        wait "$server_pid" 2>/dev/null || true
    fi
}
trap 'stop_children; exit 143' TERM
trap 'stop_children; exit 130' INT

/usr/local/bin/docker-entrypoint.sh "$@" &
server_pid=$!

# A socket alone may belong to the official entrypoint's temporary bootstrap
# server. Authenticated TCP confirms that initialization is fully finished.
ready=false
for attempt in $(seq 1 180); do
    if app_can_connect; then
        ready=true
        break
    fi
    if ! kill -0 "$server_pid" 2>/dev/null; then
        echo 'MariaDB exited before it became available.' >&2
        if wait "$server_pid"; then exit 1; else exit "$?"; fi
    fi
    sleep 1
done
if [[ "$ready" != true ]]; then
    echo 'MariaDB did not become available within three minutes.' >&2
    stop_children
    exit 1
fi

bash /opt/transitor/import-gtfs.sh &
import_pid=$!
if wait "$import_pid"; then
    import_pid=
else
    result=$?
    import_pid=
    stop_children
    exit "$result"
fi
wait "$server_pid"
