
-- table: place (gis example)
CREATE TABLE place
(
    place_id                     uuid               NOT NULL,
    created_at                   timestamp          NOT NULL,
    modified_at                  timestamp          NOT NULL,
    deleted_at                   timestamp          NULL,
    active                       bool               NOT NULL,
    place_name                   varchar(2048)      NOT NULL,
    country_name                 varchar(2048)      NOT NULL,
    city_name                    varchar(2048)      NOT NULL,
    postal_code                  varchar(2048)      NOT NULL,
    street_address               varchar(2048)      NOT NULL,
    formatted_address            varchar(2048)      NOT NULL,
    latitude                     numeric(10, 6)     NOT NULL,
    longitude                    numeric(10, 6)     NOT NULL,

    CONSTRAINT place_pkey PRIMARY KEY (place_id)
);

-- table: place_geosearch_index -> create gist-index:
CREATE INDEX place_geosearch_index ON place USING gist (ll_to_earth(latitude, longitude));
