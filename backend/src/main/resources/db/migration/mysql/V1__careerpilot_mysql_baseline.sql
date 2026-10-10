-- New MySQL installation baseline. PostgreSQL V1-V8 remain immutable source history.
-- Hibernate UUID values use BINARY(16) on MySQL; Java generates application IDs.
CREATE TABLE users (
  id BINARY(16) NOT NULL PRIMARY KEY,
  email VARCHAR(254) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  display_name VARCHAR(80) NOT NULL,
  role VARCHAR(24) NOT NULL DEFAULT 'USER',
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE jobs (
  id BINARY(16) NOT NULL PRIMARY KEY,
  title VARCHAR(160) NOT NULL,
  company VARCHAR(160) NOT NULL,
  location VARCHAR(160) NOT NULL,
  employment_type VARCHAR(40) NOT NULL,
  description TEXT NOT NULL,
  skills_text TEXT NOT NULL,
  source VARCHAR(120) NOT NULL,
  owner_user_id BINARY(16),
  is_sample BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_jobs_owner FOREIGN KEY (owner_user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT uq_user_import_job_title_company UNIQUE (owner_user_id,title,company),
  INDEX idx_jobs_title (title), INDEX idx_jobs_company (company), INDEX idx_jobs_location (location),
  INDEX idx_jobs_owner_created (owner_user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE saved_jobs (
  id BINARY(16) NOT NULL PRIMARY KEY, user_id BINARY(16) NOT NULL, job_id BINARY(16) NOT NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT uq_saved_jobs_user_job UNIQUE (user_id,job_id),
  CONSTRAINT fk_saved_jobs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_saved_jobs_job FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
  INDEX idx_saved_jobs_owner (user_id,created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE applications (
  id BINARY(16) NOT NULL PRIMARY KEY, user_id BINARY(16) NOT NULL, job_id BINARY(16),
  title VARCHAR(160) NOT NULL, company VARCHAR(160) NOT NULL, status VARCHAR(24) NOT NULL,
  applied_at TIMESTAMP(6), follow_up_at TIMESTAMP(6), notes TEXT,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT uq_applications_owner_job UNIQUE (user_id,job_id),
  CONSTRAINT fk_applications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_applications_job FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE SET NULL,
  INDEX idx_applications_owner_updated (user_id,updated_at), INDEX idx_applications_owner_status (user_id,status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE application_status_history (
  id BINARY(16) NOT NULL PRIMARY KEY, application_id BINARY(16) NOT NULL, from_status VARCHAR(24), to_status VARCHAR(24) NOT NULL,
  note VARCHAR(500), created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_status_history_application FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE,
  INDEX idx_status_history_application (application_id,created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE resumes (
  id BINARY(16) NOT NULL PRIMARY KEY, user_id BINARY(16) NOT NULL, title VARCHAR(160) NOT NULL,
  active_version_number INT NOT NULL DEFAULT 1, version INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_resumes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_resumes_owner_updated (user_id,updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE resume_versions (
  id BINARY(16) NOT NULL PRIMARY KEY, resume_id BINARY(16) NOT NULL, version_number INT NOT NULL,
  storage_key VARCHAR(80) NOT NULL UNIQUE, original_file_name VARCHAR(255) NOT NULL, content_type VARCHAR(100) NOT NULL,
  checksum_sha256 VARCHAR(64) NOT NULL, extracted_text TEXT NOT NULL, extracted_email VARCHAR(254), extracted_phone VARCHAR(48),
  skills_text TEXT NOT NULL, reviewed BOOLEAN NOT NULL DEFAULT FALSE, created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT uq_resume_version UNIQUE(resume_id,version_number),
  CONSTRAINT fk_resume_versions_resume FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE,
  INDEX idx_resume_versions_resume (resume_id,version_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE skills (
  id BINARY(16) NOT NULL PRIMARY KEY, canonical_name VARCHAR(80) NOT NULL UNIQUE, category VARCHAR(80)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
INSERT INTO skills(id,canonical_name,category) VALUES
(UUID_TO_BIN(UUID()),'Java','Programming'),(UUID_TO_BIN(UUID()),'Spring Boot','Backend'),(UUID_TO_BIN(UUID()),'JavaScript','Programming'),(UUID_TO_BIN(UUID()),'TypeScript','Programming'),(UUID_TO_BIN(UUID()),'React','Frontend'),(UUID_TO_BIN(UUID()),'Python','Programming'),(UUID_TO_BIN(UUID()),'SQL','Data'),(UUID_TO_BIN(UUID()),'PostgreSQL','Data'),(UUID_TO_BIN(UUID()),'REST APIs','Backend'),(UUID_TO_BIN(UUID()),'Docker','Infrastructure'),(UUID_TO_BIN(UUID()),'Kubernetes','Infrastructure'),(UUID_TO_BIN(UUID()),'AWS','Cloud'),(UUID_TO_BIN(UUID()),'Git','Tools'),(UUID_TO_BIN(UUID()),'Playwright','Testing'),(UUID_TO_BIN(UUID()),'Node.js','Backend'),(UUID_TO_BIN(UUID()),'HTML','Frontend'),(UUID_TO_BIN(UUID()),'CSS','Frontend'),(UUID_TO_BIN(UUID()),'Figma','Design'),(UUID_TO_BIN(UUID()),'Data Analysis','Data'),(UUID_TO_BIN(UUID()),'Machine Learning','Data'),(UUID_TO_BIN(UUID()),'Communication','Professional'),(UUID_TO_BIN(UUID()),'Project Management','Professional');

CREATE TABLE resume_job_matches (
  id BINARY(16) NOT NULL PRIMARY KEY, user_id BINARY(16) NOT NULL, resume_version_id BINARY(16) NOT NULL, job_id BINARY(16) NOT NULL,
  score INT NOT NULL, formula_version VARCHAR(32) NOT NULL, result_json JSON NOT NULL, created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT chk_match_score CHECK (score BETWEEN 0 AND 100),
  CONSTRAINT fk_matches_user FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_matches_resume FOREIGN KEY(resume_version_id) REFERENCES resume_versions(id) ON DELETE CASCADE,
  CONSTRAINT fk_matches_job FOREIGN KEY(job_id) REFERENCES jobs(id) ON DELETE CASCADE,
  INDEX idx_resume_matches_owner_date(user_id,created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE match_skills (
  id BINARY(16) NOT NULL PRIMARY KEY, match_id BINARY(16) NOT NULL, skill_id BINARY(16) NOT NULL,
  classification VARCHAR(24) NOT NULL, resume_evidence TEXT, job_evidence TEXT,
  CONSTRAINT uq_match_skill UNIQUE(match_id,skill_id),
  CONSTRAINT fk_match_skills_match FOREIGN KEY(match_id) REFERENCES resume_job_matches(id) ON DELETE CASCADE,
  CONSTRAINT fk_match_skills_skill FOREIGN KEY(skill_id) REFERENCES skills(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE cover_letters (
  id BINARY(16) NOT NULL PRIMARY KEY, user_id BINARY(16) NOT NULL, job_id BINARY(16) NOT NULL, resume_version_id BINARY(16) NOT NULL,
  application_id BINARY(16), title VARCHAR(180) NOT NULL, body TEXT NOT NULL, generation_source VARCHAR(32) NOT NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_cover_user FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_cover_job FOREIGN KEY(job_id) REFERENCES jobs(id),
  CONSTRAINT fk_cover_resume FOREIGN KEY(resume_version_id) REFERENCES resume_versions(id),
  CONSTRAINT fk_cover_application FOREIGN KEY(application_id) REFERENCES applications(id) ON DELETE SET NULL,
  INDEX idx_cover_letters_owner_date(user_id,updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE interview_questions (
  id BINARY(16) NOT NULL PRIMARY KEY, seed_key VARCHAR(100) NOT NULL UNIQUE, topic VARCHAR(40) NOT NULL, difficulty VARCHAR(24) NOT NULL,
  prompt TEXT NOT NULL, model_answer TEXT NOT NULL, rubric TEXT NOT NULL,
  INDEX idx_interview_questions_topic_difficulty(topic,difficulty)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE interview_sessions (
  id BINARY(16) NOT NULL PRIMARY KEY, user_id BINARY(16) NOT NULL, role VARCHAR(160) NOT NULL, topic VARCHAR(40) NOT NULL,
  difficulty VARCHAR(24) NOT NULL, state VARCHAR(24) NOT NULL DEFAULT 'IN_PROGRESS', started_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  ended_at TIMESTAMP(6), average_score INT,
  CONSTRAINT fk_interview_sessions_user FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_interview_sessions_owner_date(user_id,started_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE interview_answers (
  id BINARY(16) NOT NULL PRIMARY KEY, session_id BINARY(16) NOT NULL, question_id BINARY(16) NOT NULL, ordinal INT NOT NULL,
  answer TEXT NOT NULL, content_coverage_score INT NOT NULL, feedback TEXT NOT NULL, created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT uq_interview_answer_ordinal UNIQUE(session_id,ordinal), CONSTRAINT uq_interview_answer_question UNIQUE(session_id,question_id),
  CONSTRAINT chk_interview_score CHECK(content_coverage_score BETWEEN 0 AND 100),
  CONSTRAINT fk_interview_answers_session FOREIGN KEY(session_id) REFERENCES interview_sessions(id) ON DELETE CASCADE,
  CONSTRAINT fk_interview_answers_question FOREIGN KEY(question_id) REFERENCES interview_questions(id),
  INDEX idx_interview_answers_session(session_id,ordinal)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE user_profiles (
  id BINARY(16) NOT NULL PRIMARY KEY, user_id BINARY(16) NOT NULL UNIQUE, headline VARCHAR(180), location VARCHAR(160), summary TEXT,
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), version INT NOT NULL DEFAULT 0,
  CONSTRAINT fk_profiles_user FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE simulated_portal_submissions (
  id BINARY(16) NOT NULL PRIMARY KEY, owner_user_id BINARY(16) NOT NULL, portal_key VARCHAR(24) NOT NULL, job_key VARCHAR(80) NOT NULL,
  job_title VARCHAR(160) NOT NULL, applicant_name VARCHAR(120) NOT NULL, applicant_email VARCHAR(254) NOT NULL, education VARCHAR(300),
  experience TEXT, cover_letter TEXT, resume_file_name VARCHAR(255), resume_bytes LONGBLOB, answers_json TEXT NOT NULL,
  idempotency_key BINARY(16) NOT NULL, confirmation_id BINARY(16) NOT NULL UNIQUE, created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT chk_portal_key CHECK(portal_key IN ('PORTAL_A','PORTAL_B','PORTAL_C')),
  CONSTRAINT uq_simulated_submission_request UNIQUE(owner_user_id,portal_key,idempotency_key),
  CONSTRAINT uq_simulated_submission_identity UNIQUE(owner_user_id,portal_key,job_key,applicant_email),
  CONSTRAINT fk_submission_owner FOREIGN KEY(owner_user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_simulated_submission_created(created_at), INDEX idx_simulated_submission_owner(owner_user_id,created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE automation_runs (
  id BINARY(16) NOT NULL PRIMARY KEY, owner_user_id BINARY(16) NOT NULL, portal_key VARCHAR(24) NOT NULL, job_key VARCHAR(80) NOT NULL,
  status VARCHAR(24) NOT NULL, idempotency_key BINARY(16) NOT NULL, request_fingerprint VARCHAR(64) NOT NULL,
  confirmation_id BINARY(16), result_message VARCHAR(500), created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  started_at TIMESTAMP(6), completed_at TIMESTAMP(6),
  CONSTRAINT chk_automation_portal CHECK(portal_key IN ('PORTAL_A','PORTAL_B','PORTAL_C')),
  CONSTRAINT chk_automation_status CHECK(status IN ('PENDING','RUNNING','VERIFIED_SUCCESS','FAILED','UNVERIFIED')),
  CONSTRAINT uq_automation_run_owner_idempotency UNIQUE(owner_user_id,idempotency_key),
  CONSTRAINT fk_automation_owner FOREIGN KEY(owner_user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_automation_confirmation FOREIGN KEY(confirmation_id) REFERENCES simulated_portal_submissions(confirmation_id) ON DELETE SET NULL,
  INDEX idx_automation_run_owner_created(owner_user_id,created_at), INDEX idx_automation_run_status_created(status,created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO jobs(id,title,company,location,employment_type,description,skills_text,source,is_sample) VALUES
(UUID_TO_BIN(UUID()),'Junior Java Engineer','Northstar Labs','Bengaluru, India','FULL_TIME','Build reliable services and APIs with a supportive product engineering team. You will work with Java, Spring Boot, PostgreSQL, and code review.','Java, Spring Boot, REST APIs, PostgreSQL, Git','CareerPilot sample data',TRUE),
(UUID_TO_BIN(UUID()),'Frontend Developer','Studio Meridian','Remote · India','FULL_TIME','Help create accessible web experiences for a growing collaboration product using React, TypeScript, design systems, and modern testing practices.','React, TypeScript, CSS, Accessibility, Testing','CareerPilot sample data',TRUE),
(UUID_TO_BIN(UUID()),'Product Data Analyst','Brightpath Health','Pune, India','FULL_TIME','Partner with product teams to understand user behavior, define meaningful metrics, and build clear analysis with SQL and visualization tools.','SQL, Python, Statistics, Data Visualization','CareerPilot sample data',TRUE),
(UUID_TO_BIN(UUID()),'QA Automation Associate','Cloudcraft Systems','Hyderabad, India','FULL_TIME','Improve release confidence by designing practical automated checks across API and browser workflows in collaboration with product engineers.','Playwright, Java, API Testing, CI/CD','CareerPilot sample data',TRUE),
(UUID_TO_BIN(UUID()),'Associate UX Designer','Fieldnote','Remote · India','CONTRACT','Shape onboarding and core product flows with research, prototyping, and close collaboration across product and engineering.','Figma, User Research, Prototyping, Accessibility','CareerPilot sample data',TRUE),
(UUID_TO_BIN(UUID()),'Backend Developer Intern','Monsoon Mobility','Chennai, India','INTERNSHIP','Contribute to backend services and learn production practices while building APIs, integrating databases, and improving observability.','Java, Spring Boot, SQL, REST APIs','CareerPilot sample data',TRUE);

INSERT INTO interview_questions(id,seed_key,topic,difficulty,prompt,model_answer,rubric) VALUES
(UUID_TO_BIN(UUID()),'java-oop-encapsulation','JAVA','BEGINNER','What is encapsulation in Java, and how do access modifiers support it?','Encapsulation bundles state and behavior while controlling access to implementation details. Private fields with validated public methods protect invariants; package, protected, and public visibility define the intended boundary.','Look for definition, controlled access, and a concrete invariant or example.'),
(UUID_TO_BIN(UUID()),'java-equals-hashcode','JAVA','INTERMEDIATE','What contract should equals and hashCode follow when objects are used as HashMap keys?','Equal objects must return the same hash code. hashCode must remain stable while equality-relevant state is unchanged. HashMap uses the hash to choose a bucket and equals to identify a matching key.','Look for equal-implies-same-hash and the role of both methods.'),
(UUID_TO_BIN(UUID()),'spring-di','SPRING_BOOT','BEGINNER','What is dependency injection, and why is constructor injection usually preferred in Spring?','Dependency injection supplies a component its collaborators instead of constructing them internally. Constructor injection makes required dependencies explicit, supports immutability, and simplifies unit tests.','Look for inversion of control, explicit dependencies, and testability.'),
(UUID_TO_BIN(UUID()),'spring-transactions','SPRING_BOOT','ADVANCED','How does Spring transaction propagation affect a service method called inside another transaction?','Propagation defines whether a method joins, suspends, or creates a transaction. REQUIRED joins or creates one; REQUIRES_NEW suspends the current transaction and starts another. Proxy boundaries and rollback rules matter.','Look for REQUIRED and REQUIRES_NEW semantics and proxy/rollback caveats.'),
(UUID_TO_BIN(UUID()),'sql-index','SQL','BEGINNER','What does a database index do, and what is one tradeoff of adding an index?','An index helps the database locate rows without scanning every row. It uses storage and adds work to inserts, updates, and deletes, so indexes should support measured query patterns.','Look for lookup benefit and write/storage cost.'),
(UUID_TO_BIN(UUID()),'sql-joins','SQL','INTERMEDIATE','Explain the difference between an INNER JOIN and a LEFT JOIN.','INNER JOIN returns rows with matches on both sides. LEFT JOIN preserves every left-side row and fills right-side columns with null when no match exists.','Look for preservation of unmatched left rows.'),
(UUID_TO_BIN(UUID()),'dbms-acid','DBMS','INTERMEDIATE','Describe the ACID properties of a database transaction.','Atomicity groups operations as all-or-nothing; consistency preserves constraints; isolation limits interference among concurrent transactions; durability keeps committed work after failure.','Look for all four properties with accurate meanings.'),
(UUID_TO_BIN(UUID()),'behavioral-star','BEHAVIORAL','BEGINNER','Tell me about a difficult problem you solved. How would you structure your answer?','A clear response gives the situation and task, describes personal actions and reasoning, and finishes with a result and a reflection. A specific, truthful example is more useful than a general claim.','Look for a specific situation, the candidate’s actions, an outcome, and a reflection.');

INSERT INTO interview_questions(id,seed_key,topic,difficulty,prompt,model_answer,rubric) VALUES
(UUID_TO_BIN(UUID()),'dsa-hashmap','DSA','INTERMEDIATE','How does a hash map handle collisions, and what affects its performance?','A hash map maps a key hash to a bucket and resolves collisions using a structure such as a list or tree. Good hash distribution and a controlled load factor keep lookup near constant time on average; pathological collisions can make it slower.','Cover buckets, collision handling, load factor, and average-versus-worst-case behavior.'),
(UUID_TO_BIN(UUID()),'dsa-complexity','DSA','BEGINNER','What is the time complexity of binary search, and what condition does it require?','Binary search takes O(log n) comparisons because it halves the candidate range each step. The input must be ordered, and the searched range must support efficient midpoint access for the usual array implementation.','Identify logarithmic complexity and sorted input.'),
(UUID_TO_BIN(UUID()),'oop-solid','OOP','ADVANCED','Choose one SOLID principle and explain a design change that follows it.','For example, the open/closed principle suggests extending behavior through a stable abstraction rather than repeatedly editing a central conditional. A strategy interface can let new pricing rules be added without changing existing callers.','Explain a principle, concrete design, and its tradeoff.'),
(UUID_TO_BIN(UUID()),'os-process-thread','OPERATING_SYSTEMS','BEGINNER','How does a process differ from a thread?','A process has its own virtual address space and operating-system resources. Threads execute within a process and share its memory while retaining their own stacks and scheduling state.','Mention process isolation and shared process memory among threads.'),
(UUID_TO_BIN(UUID()),'os-deadlock','OPERATING_SYSTEMS','INTERMEDIATE','What conditions are necessary for deadlock, and how can a system reduce its risk?','Deadlock requires mutual exclusion, hold-and-wait, no preemption, and circular wait. Consistent lock ordering can prevent circular wait; timeouts or detection and recovery are other approaches.','Name all four conditions and one mitigation.'),
(UUID_TO_BIN(UUID()),'net-http','NETWORKS','BEGINNER','What is the difference between TCP and UDP?','TCP provides a reliable ordered byte stream with connection management and retransmission. UDP sends datagrams without those guarantees and has lower protocol overhead, so applications choose based on their delivery needs.','Compare reliability/order with datagram-oriented delivery.'),
(UUID_TO_BIN(UUID()),'net-tls','NETWORKS','ADVANCED','At a high level, what does TLS provide to an HTTPS connection?','TLS negotiates cryptographic parameters, authenticates the server using certificates, and establishes keys for encrypted and integrity-protected communication. Correct certificate validation is essential to prevent impersonation.','Mention confidentiality, integrity, authentication, and certificate validation.'),
(UUID_TO_BIN(UUID()),'rest-idempotency','REST_APIS','INTERMEDIATE','What does idempotency mean for an HTTP operation, and why does it matter for retries?','An idempotent operation has the same intended effect when repeated. Safe retries after a timeout can avoid duplicate changes; a server may use an idempotency key for non-idempotent actions such as creating a submission.','Define repeated effect and connect it to safe retries.'),
(UUID_TO_BIN(UUID()),'rest-status','REST_APIS','BEGINNER','When would an API return 201, 204, and 409?','201 indicates a resource was created, 204 indicates successful completion with no response body, and 409 indicates a conflict with the current resource state such as a duplicate request.','Give the meaning of each status code.'),
(UUID_TO_BIN(UUID()),'system-cache','SYSTEM_DESIGN','INTERMEDIATE','What tradeoffs should a team consider before adding a cache?','A cache can reduce latency and load but introduces stale data, invalidation, memory cost, and another failure mode. Choose the data and consistency window, keying strategy, eviction policy, and fallback behavior deliberately.','Balance latency/load benefits against staleness, invalidation, and failures.'),
(UUID_TO_BIN(UUID()),'system-scale','SYSTEM_DESIGN','ADVANCED','How would you approach a service that is becoming slow as traffic grows?','Measure first with request and database metrics, identify the bottleneck, and establish a target. Depending on evidence, optimize queries, add indexes or caching, scale stateless instances, and use queues for asynchronous work while preserving correctness.','Ask for measurement and bottleneck evidence before proposing changes.'),
(UUID_TO_BIN(UUID()),'behavioral-conflict','BEHAVIORAL','INTERMEDIATE','Describe a disagreement with a teammate and how you worked through it.','A strong response gives a concrete context, listens to the other person, explains the evidence and options, agrees on a decision or experiment, and reflects on the result.','Look for respectful listening, concrete actions, outcome, and reflection.'),
(UUID_TO_BIN(UUID()),'java-collections','JAVA','BEGINNER','When would you choose a Set rather than a List?','A Set is useful when uniqueness and membership checks matter. A List preserves order and allows duplicates. The right choice depends on required behavior and iteration/access patterns.','Compare uniqueness/membership with order/duplicates.'),
(UUID_TO_BIN(UUID()),'sql-transactions','SQL','ADVANCED','Why can two concurrent transactions produce a lost update, and how can it be prevented?','Both transactions can read the same value and then overwrite each other. Optimistic version checks, row locks, or an isolation level that detects the conflict can prevent silent loss; retry behavior must be considered.','Explain the race and name a concurrency control option.');
