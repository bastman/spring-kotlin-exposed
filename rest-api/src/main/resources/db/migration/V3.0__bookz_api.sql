CREATE TABLE bookz (
  id         UUID                        NOT NULL,
  created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  is_active BOOLEAN NOT NULL,
  data       JSONB                       NOT NULL
);
ALTER TABLE bookz
  OWNER TO app_rw;
ALTER TABLE ONLY bookz
  ADD CONSTRAINT bookz_pkey PRIMARY KEY (id);

