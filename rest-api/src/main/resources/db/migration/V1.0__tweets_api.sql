CREATE TABLE tweet (
  id         UUID                        NOT NULL,
  version    INTEGER                     NOT NULL,
  created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  message    TEXT                        NOT NULL,
  comment    TEXT                        NULL
);
ALTER TABLE tweet
  OWNER TO app_rw;
ALTER TABLE ONLY tweet
  ADD CONSTRAINT tweet_pkey PRIMARY KEY (id);
