CREATE TABLE resumes (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  title VARCHAR(160) NOT NULL,
  active_version_number INTEGER NOT NULL DEFAULT 1,
  version INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_resumes_owner_updated ON resumes (user_id, updated_at DESC);

CREATE TABLE resume_versions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  resume_id UUID NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
  version_number INTEGER NOT NULL,
  storage_key VARCHAR(80) NOT NULL UNIQUE,
  original_file_name VARCHAR(255) NOT NULL,
  content_type VARCHAR(100) NOT NULL,
  checksum_sha256 CHAR(64) NOT NULL,
  extracted_text TEXT NOT NULL,
  extracted_email VARCHAR(254),
  extracted_phone VARCHAR(48),
  skills_text TEXT NOT NULL DEFAULT '',
  reviewed BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_resume_version UNIQUE (resume_id, version_number)
);
CREATE INDEX idx_resume_versions_resume ON resume_versions (resume_id, version_number DESC);

CREATE TABLE skills (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  canonical_name VARCHAR(80) NOT NULL UNIQUE,
  category VARCHAR(80)
);
INSERT INTO skills (canonical_name,category) VALUES
('Java','Programming'),('Spring Boot','Backend'),('JavaScript','Programming'),('TypeScript','Programming'),('React','Frontend'),
('Python','Programming'),('SQL','Data'),('PostgreSQL','Data'),('REST APIs','Backend'),('Docker','Infrastructure'),
('Kubernetes','Infrastructure'),('AWS','Cloud'),('Git','Tools'),('Playwright','Testing'),('Node.js','Backend'),
('HTML','Frontend'),('CSS','Frontend'),('Figma','Design'),('Data Analysis','Data'),('Machine Learning','Data'),
('Communication','Professional'),('Project Management','Professional');

CREATE TABLE resume_job_matches (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  resume_version_id UUID NOT NULL REFERENCES resume_versions(id) ON DELETE CASCADE,
  job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
  score INTEGER NOT NULL CHECK (score BETWEEN 0 AND 100),
  formula_version VARCHAR(32) NOT NULL,
  result_json JSONB NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_resume_matches_owner_date ON resume_job_matches (user_id, created_at DESC);

CREATE TABLE match_skills (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  match_id UUID NOT NULL REFERENCES resume_job_matches(id) ON DELETE CASCADE,
  skill_id UUID NOT NULL REFERENCES skills(id),
  classification VARCHAR(24) NOT NULL,
  resume_evidence TEXT,
  job_evidence TEXT,
  CONSTRAINT uq_match_skill UNIQUE (match_id, skill_id)
);
