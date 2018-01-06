#!/bin/sh
set -eo pipefail

host="$(hostname -i || echo '127.0.0.1')"
curl --fail "http://$host/health" || exit 1
