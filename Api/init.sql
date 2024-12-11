create schema if not exists scanner;

create table if not exists accelerationValues (
    id serial primary key,
    x double precision,
    y double precision,
    z double precision,
    timestamp timestamp
);

create table if not exists orientationValues (
    id serial primary key,
    x double precision,
    y double precision,
    z double precision,
    timestamp timestamp
);

-- Grant SELECT, INSERT, UPDATE, and DELETE permissions on tables
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE scanner.accelerationValues TO public;

-- Grant USAGE on the schema
GRANT USAGE ON SCHEMA scanner TO public;
