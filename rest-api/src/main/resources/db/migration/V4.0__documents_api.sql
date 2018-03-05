CREATE TABLE documents (
  id         CHARACTER VARYING(255)      NOT NULL,
  created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  data       JSONB                       NOT NULL
);
ALTER TABLE documents
  OWNER TO app_rw;
ALTER TABLE ONLY documents
  ADD CONSTRAINT documents_pkey PRIMARY KEY (id);

