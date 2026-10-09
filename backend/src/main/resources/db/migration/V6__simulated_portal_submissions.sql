CREATE TABLE simulated_portal_submissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    portal_key VARCHAR(24) NOT NULL CHECK (portal_key IN ('PORTAL_A', 'PORTAL_B', 'PORTAL_C')),
    job_key VARCHAR(80) NOT NULL,
    job_title VARCHAR(160) NOT NULL,
    applicant_name VARCHAR(120) NOT NULL,
    applicant_email VARCHAR(254) NOT NULL,
    resume_file_name VARCHAR(255),
    resume_bytes BYTEA,
    answers_json TEXT NOT NULL,
    idempotency_key UUID NOT NULL,
    confirmation_id UUID NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_simulated_submission_request UNIQUE (owner_user_id, portal_key, idempotency_key)
);
CREATE INDEX idx_simulated_submission_created ON simulated_portal_submissions(created_at DESC);
CREATE INDEX idx_simulated_submission_owner ON simulated_portal_submissions(owner_user_id, created_at DESC);
