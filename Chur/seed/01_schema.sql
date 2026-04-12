-- ArcadeDB seed: User document type
-- Run once against the 'lineage' database.
-- Execute via: curl -u root:playwithdata -X POST http://localhost:2480/api/v1/command/lineage \
--   -H 'Content-Type: application/json' \
--   -d '{"language":"sql","command":"<paste each statement>"}'

CREATE DOCUMENT TYPE User IF NOT EXISTS;
CREATE PROPERTY User.username IF NOT EXISTS STRING;
CREATE PROPERTY User.password_hash IF NOT EXISTS STRING;
CREATE PROPERTY User.role IF NOT EXISTS STRING;
CREATE INDEX IF NOT EXISTS ON User (username) UNIQUE;
