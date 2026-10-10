CREATE TABLE automation_runs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    portal_key VARCHAR(24) NOT NULL CHECK (portal_key IN ('PORTAL_A', 'PORTAL_B', 'PORTAL_C')),
    job_key VARCHAR(80) NOT NULL,
    status VARCHAR(24) NOT NULL CHECK (status IN ('PENDING', 'RUNNING', 'VERIFIED_SUCCESS', 'FAILED', 'UNVERIFIED')),
    idempotency_key UUID NOT NULL,
    request_fingerprint VARCHAR(64) NOT NULL,
    confirmation_id UUID REFERENCES simulated_portal_submissions(confirmation_id) ON DELETE SET NULL,
    result_message VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    CONSTRAINT uq_automation_run_owner_idempotency UNIQUE (owner_user_id, idempotency_key)
);
CREATE INDEX idx_automation_run_owner_created ON automation_runs(owner_user_id, created_at DESC);
CREATE INDEX idx_automation_run_status_created ON automation_runs(status, created_at DESC);
