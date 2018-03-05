CREATE TABLE author (
  id         UUID                        NOT NULL,
  version    INTEGER                     NOT NULL,
  created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  name       TEXT                        NOT NULL
);
ALTER TABLE author
  OWNER TO app_rw;
ALTER TABLE ONLY author
  ADD CONSTRAINT author_pkey PRIMARY KEY (id);

CREATE TABLE book (
  id         UUID                        NOT NULL,
  author_id  UUID                        NOT NULL,
  version    INTEGER                     NOT NULL,
  created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  title      CHARACTER VARYING(255)      NOT NULL,
  status     CHARACTER VARYING(255)      NOT NULL,
  price      NUMERIC(15, 2)              NOT NULL
);
ALTER TABLE book
  OWNER TO app_rw;
ALTER TABLE ONLY book
  ADD CONSTRAINT book_pkey PRIMARY KEY (id);
ALTER TABLE ONLY book
  ADD CONSTRAINT book_author_id_fkey FOREIGN KEY (author_id) REFERENCES author (id);
