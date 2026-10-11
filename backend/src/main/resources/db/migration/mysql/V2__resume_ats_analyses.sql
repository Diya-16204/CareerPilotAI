-- Persist only explainable analysis output and ownership references; resume content remains in resume_versions.
CREATE TABLE resume_analyses (
  id BINARY(16) NOT NULL PRIMARY KEY,
  user_id BINARY(16) NOT NULL,
  resume_version_id BINARY(16) NOT NULL,
  job_id BINARY(16) NULL,
  score INT NOT NULL,
  analysis_type VARCHAR(24) NOT NULL,
  formula_version VARCHAR(40) NOT NULL,
  result_json LONGTEXT NOT NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_resume_analyses_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_resume_analyses_version FOREIGN KEY (resume_version_id) REFERENCES resume_versions(id) ON DELETE CASCADE,
  CONSTRAINT fk_resume_analyses_job FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE SET NULL,
  CONSTRAINT chk_resume_analyses_score CHECK (score BETWEEN 0 AND 100),
  INDEX idx_resume_analysis_owner_created (user_id,created_at),
  INDEX idx_resume_analysis_version_created (resume_version_id,created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
