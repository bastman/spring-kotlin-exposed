CREATE TYPE TweetStatusType AS ENUM ('DRAFT', 'PENDING', 'PUBLISHED');

CREATE TABLE Tweet (
  id         UUID                        NOT NULL,
  version    INTEGER                     NOT NULL,
  created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  deleted_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT TIMESTAMP 'epoch',

  status TweetStatusType NOT NULL DEFAULT 'DRAFT',
  message    CHARACTER VARYING(255) NOT NULL,
  comment    TEXT                        NULL
);
ALTER TABLE Tweet
  OWNER TO app_rw;
ALTER TABLE ONLY Tweet
  ADD CONSTRAINT tweet_pkey PRIMARY KEY (id);
