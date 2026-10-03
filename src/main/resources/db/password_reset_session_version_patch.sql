-- Run before deploying session-version-aware backend instances.
BEGIN;
ALTER TABLE users ADD COLUMN IF NOT EXISTS session_version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN IF NOT EXISTS row_version BIGINT NOT NULL DEFAULT 0;
COMMIT;
