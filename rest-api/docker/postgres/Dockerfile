FROM library/postgres:9.6.3-alpine

EXPOSE 5432

COPY docker/postgres/docker-entrypoint-initdb.d/** /docker-entrypoint-initdb.d/
COPY docker/postgres/db-dumps/** /db-dumps/




