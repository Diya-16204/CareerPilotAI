CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email VARCHAR(254) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  display_name VARCHAR(80) NOT NULL,
  role VARCHAR(24) NOT NULL DEFAULT 'USER',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE jobs (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  title VARCHAR(160) NOT NULL,
  company VARCHAR(160) NOT NULL,
  location VARCHAR(160) NOT NULL,
  employment_type VARCHAR(40) NOT NULL,
  description TEXT NOT NULL,
  skills_text TEXT NOT NULL DEFAULT '',
  source VARCHAR(120) NOT NULL,
  source_key VARCHAR(200),
  is_sample BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_jobs_source_key UNIQUE (source, source_key)
);
CREATE INDEX idx_jobs_title ON jobs (lower(title));
CREATE INDEX idx_jobs_company ON jobs (lower(company));
CREATE INDEX idx_jobs_location ON jobs (lower(location));

CREATE TABLE saved_jobs (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_saved_jobs_user_job UNIQUE (user_id, job_id)
);
CREATE INDEX idx_saved_jobs_owner ON saved_jobs (user_id, created_at DESC);

CREATE TABLE applications (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  job_id UUID REFERENCES jobs(id) ON DELETE SET NULL,
  title VARCHAR(160) NOT NULL,
  company VARCHAR(160) NOT NULL,
  status VARCHAR(24) NOT NULL,
  applied_at TIMESTAMPTZ,
  follow_up_at TIMESTAMPTZ,
  notes TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_applications_owner_job UNIQUE (user_id, job_id)
);
CREATE INDEX idx_applications_owner_updated ON applications (user_id, updated_at DESC);
CREATE INDEX idx_applications_owner_status ON applications (user_id, status);

CREATE TABLE application_status_history (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  application_id UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
  from_status VARCHAR(24),
  to_status VARCHAR(24) NOT NULL,
  note VARCHAR(500),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_status_history_application ON application_status_history (application_id, created_at DESC);

INSERT INTO jobs (title, company, location, employment_type, description, skills_text, source, source_key, is_sample) VALUES
('Junior Java Engineer', 'Northstar Labs', 'Bengaluru, India', 'FULL_TIME', 'Build reliable services and APIs with a supportive product engineering team. You will work with Java, Spring Boot, PostgreSQL, and code review.', 'Java, Spring Boot, REST APIs, PostgreSQL, Git', 'CareerPilot sample data', 'sample-java-engineer', true),
('Frontend Developer', 'Studio Meridian', 'Remote · India', 'FULL_TIME', 'Help create accessible web experiences for a growing collaboration product using React, TypeScript, design systems, and modern testing practices.', 'React, TypeScript, CSS, Accessibility, Testing', 'CareerPilot sample data', 'sample-frontend-dev', true),
('Product Data Analyst', 'Brightpath Health', 'Pune, India', 'FULL_TIME', 'Partner with product teams to understand user behavior, define meaningful metrics, and build clear analysis with SQL and visualization tools.', 'SQL, Python, Statistics, Data Visualization', 'CareerPilot sample data', 'sample-data-analyst', true),
('QA Automation Associate', 'Cloudcraft Systems', 'Hyderabad, India', 'FULL_TIME', 'Improve release confidence by designing practical automated checks across API and browser workflows in collaboration with product engineers.', 'Playwright, Java, API Testing, CI/CD', 'CareerPilot sample data', 'sample-qa-automation', true),
('Associate UX Designer', 'Fieldnote', 'Remote · India', 'CONTRACT', 'Shape onboarding and core product flows with research, prototyping, and close collaboration across product and engineering.', 'Figma, User Research, Prototyping, Accessibility', 'CareerPilot sample data', 'sample-ux-designer', true),
('Backend Developer Intern', 'Monsoon Mobility', 'Chennai, India', 'INTERNSHIP', 'Contribute to backend services and learn production practices while building APIs, integrating databases, and improving observability.', 'Java, Spring Boot, SQL, REST APIs', 'CareerPilot sample data', 'sample-backend-intern', true);
