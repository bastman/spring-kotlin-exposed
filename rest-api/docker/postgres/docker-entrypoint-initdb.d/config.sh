#!/bin/bash
# https://github.com/Roconda/docker-postgres-logging/blame/master/config.sh

echo "patch config - enable statment logs: log_statement=all in /var/lib/postgresql/data/postgresql.conf..."
set -e

sed -ri "s/#log_statement = 'none'/log_statement = 'all'/g" /var/lib/postgresql/data/postgresql.conf
