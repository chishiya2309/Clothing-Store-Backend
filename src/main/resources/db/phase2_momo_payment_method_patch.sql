-- ============================================================
-- PHASE 2 PATCH: MoMo payment method
-- Safe to run on an existing PostgreSQL database.
-- ============================================================

ALTER TYPE payment_method ADD VALUE IF NOT EXISTS 'momo';
