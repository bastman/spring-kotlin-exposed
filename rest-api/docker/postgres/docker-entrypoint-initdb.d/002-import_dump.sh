#!/usr/bin/env sh

echo "======= import sql dump ======="

psql app -f /db-dumps/dump.sql
