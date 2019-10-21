#!/usr/bin/env sh

echo "======= install sql extensions ======="

psql app -f /db-dumps/extensions.sql
psql app_test -f /db-dumps/extensions.sql

