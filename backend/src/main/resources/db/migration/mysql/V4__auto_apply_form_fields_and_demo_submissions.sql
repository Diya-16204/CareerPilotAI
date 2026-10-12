ALTER TABLE application_prep_sessions
  ADD COLUMN application_fields_json LONGTEXT NULL;

UPDATE application_prep_sessions
SET application_fields_json = '[]'
WHERE application_fields_json IS NULL;

ALTER TABLE application_prep_sessions
  MODIFY COLUMN application_fields_json LONGTEXT NOT NULL;

CREATE TABLE auto_apply_demo_submissions (
  id BINARY(16) NOT NULL PRIMARY KEY,
  owner_user_id BINARY(16) NOT NULL,
  session_id BINARY(16) NOT NULL,
  job_title VARCHAR(200) NOT NULL,
  company VARCHAR(200) NOT NULL,
  resume_file_name VARCHAR(255) NOT NULL,
  cover_letter LONGTEXT NOT NULL,
  application_url VARCHAR(1000) NULL,
  application_fields_json LONGTEXT NOT NULL,
  submitted_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT uq_auto_apply_demo_session UNIQUE (session_id),
  CONSTRAINT fk_auto_apply_demo_owner FOREIGN KEY (owner_user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_auto_apply_demo_session FOREIGN KEY (session_id) REFERENCES application_prep_sessions(id) ON DELETE CASCADE,
  INDEX idx_auto_apply_demo_owner_submitted (owner_user_id, submitted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
