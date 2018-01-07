--
-- PostgreSQL database dump
--

-- Dumped from database version 9.6.3
-- Dumped by pg_dump version 9.6.5

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;


CREATE TABLE tweet (
    id uuid NOT NULL,
    version integer NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    message text NOT NULL,
    comment text NULL
);
ALTER TABLE tweet OWNER TO app_rw;
ALTER TABLE ONLY tweet
    ADD CONSTRAINT tweet_pkey PRIMARY KEY (id);


CREATE TABLE author (
    id uuid NOT NULL,
    version integer NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    name text NOT NULL
);
ALTER TABLE author OWNER TO app_rw;
ALTER TABLE ONLY author
    ADD CONSTRAINT author_pkey PRIMARY KEY (id);

CREATE TABLE book (
    id uuid NOT NULL,
    author_id uuid NOT NULL,
    version integer NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    title character varying(255) NOT NULL,
    status character varying(255) NOT NULL,
    price numeric(15,2) NOT NULL
);
ALTER TABLE book OWNER TO app_rw;
ALTER TABLE ONLY book
    ADD CONSTRAINT book_pkey PRIMARY KEY (id);
ALTER TABLE ONLY book
    ADD CONSTRAINT book_author_id_fkey FOREIGN KEY (author_id) REFERENCES author(id);
--
-- PostgreSQL database dump complete
--

