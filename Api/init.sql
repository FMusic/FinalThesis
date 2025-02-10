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

create table if not exists accelerationFiltered (
    id serial primary key,
    predictionX double precision,
    predictionY double precision,
    predictionZ double precision,
    filteredX double precision,
    filteredY double precision,
    filteredZ double precision,
    normalizedPrediction DOUBLE PRECISION,
    normalizedFiltered DOUBLE PRECISION,
    timestamp BIGINT
);

create table if not exists accelerationFiltered3d(
    id serial primary key,
    predictionX double precision,
    predictionY double precision,
    predictionZ double precision,
    filteredX double precision,
    filteredY double precision,
    filteredZ double precision,
    normalizedPrediction DOUBLE PRECISION,
    normalizedFiltered DOUBLE PRECISION,
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
    stepTimestamp BIGINT,
    stepLength DOUBLE PRECISION,
    velocity DOUBLE PRECISION,
    posX DOUBLE PRECISION,
    posY DOUBLE PRECISION,
    posZ DOUBLE PRECISION,
    cumulativeDistance DOUBLE PRECISION,
    -- add more if needed
    createdAt TIMESTAMP DEFAULT now()
);



-- Grant SELECT, INSERT, UPDATE, and DELETE permissions on tables
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE accelerationValues TO public;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE accelerationFiltered TO public;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE accelerationFiltered3d TO public;

GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE orientationValues TO public;

-- Grant USAGE on the schema
GRANT USAGE ON SCHEMA scanner TO public;
