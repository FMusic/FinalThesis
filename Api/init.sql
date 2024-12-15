-- Ensure the schema exists
CREATE SCHEMA IF NOT EXISTS scanner;

-- Set the search path to the scanner schema
SET search_path TO scanner;

-- Create tables in the scanner schema
CREATE TABLE IF NOT EXISTS accelerationValues (
    id SERIAL PRIMARY KEY,
    x DOUBLE PRECISION,
    y DOUBLE PRECISION,
    z DOUBLE PRECISION,
    timestamp TIMESTAMP
);

CREATE TABLE IF NOT EXISTS orientationValues (
    id SERIAL PRIMARY KEY,
    x DOUBLE PRECISION,
    y DOUBLE PRECISION,
    z DOUBLE PRECISION,
    timestamp TIMESTAMP
);

create table if not exists accelerationFiltered (
    id serial primary key,
    x double precision,
    y double precision,
    z double precision,
    timestamp timestamp
);

create table if not exists orientationFiltered (
    id serial primary key,
    x double precision,
    y double precision,
    z double precision,
    timestamp timestamp
);

create table if not exists accelerationFiltered3d(
    id serial primary key,
    x double precision,
    timestamp timestamp
);

create table if not exists orientationFiltered3d(
    id serial primary key,
    x double precision,
    timestamp timestamp
);

-- Grant SELECT, INSERT, UPDATE, and DELETE permissions on tables
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE accelerationValues TO public;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE orientationValues TO public;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE calibratedAccelerationValues TO public;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE calibratedOrientationValues TO public;

-- Grant USAGE on the schema
GRANT USAGE ON SCHEMA scanner TO public;
