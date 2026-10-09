CREATE TABLE user_profiles (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
  headline VARCHAR(180),
  location VARCHAR(160),
  summary TEXT,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  version INTEGER NOT NULL DEFAULT 0
);

ALTER TABLE jobs ADD COLUMN owner_user_id UUID REFERENCES users(id) ON DELETE CASCADE;
CREATE INDEX idx_jobs_owner_created ON jobs (owner_user_id,created_at DESC);
CREATE UNIQUE INDEX uq_user_import_job_title_company ON jobs (owner_user_id,lower(title),lower(company)) WHERE owner_user_id IS NOT NULL;
