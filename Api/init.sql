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
    normalization DOUBLE PRECISION,
    timestamp BIGINT
);

CREATE TABLE IF NOT EXISTS orientationValues (
    id SERIAL PRIMARY KEY,
    x DOUBLE PRECISION,
    y DOUBLE PRECISION,
    z DOUBLE PRECISION,
    w DOUBLE PRECISION,
    azimuth DOUBLE PRECISION,
    timestamp BIGINT
);

CREATE TABLE IF NOT EXISTS stepEvents (
    id SERIAL PRIMARY KEY,
    steplength DOUBLE PRECISION,
    velocity DOUBLE PRECISION,
    posx DOUBLE PRECISION,
    posy DOUBLE PRECISION,
    posz DOUBLE PRECISION,
    timestamp BIGINT
);

CREATE TABLE IF NOT EXISTS calibrator (
    id SERIAL PRIMARY KEY,
    maxacc DOUBLE PRECISION,
    minacc DOUBLE PRECISION,
    timestampmax BIGINT,
    timestampmin BIGINT
);

CREATE TABLE IF NOT EXISTS accelerationFiltered (
    id SERIAL PRIMARY KEY,
    x DOUBLE PRECISION,
    y DOUBLE PRECISION,
    z DOUBLE PRECISION,
    normalization DOUBLE PRECISION,
    timestamp BIGINT
);

-- Grant SELECT, INSERT, UPDATE, and DELETE permissions on tables
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE accelerationFiltered TO public;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE accelerationValues TO public;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE orientationValues TO public;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE stepEvents TO public;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE calibrator TO public;

-- Grant USAGE on the schema
GRANT USAGE ON SCHEMA scanner TO public;
