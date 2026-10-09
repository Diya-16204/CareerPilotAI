CREATE TABLE cover_letters (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  job_id UUID NOT NULL REFERENCES jobs(id),
  resume_version_id UUID NOT NULL REFERENCES resume_versions(id),
  application_id UUID REFERENCES applications(id) ON DELETE SET NULL,
  title VARCHAR(180) NOT NULL,
  body TEXT NOT NULL,
  generation_source VARCHAR(32) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_cover_letters_owner_date ON cover_letters (user_id, updated_at DESC);

CREATE TABLE interview_questions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  seed_key VARCHAR(100) NOT NULL UNIQUE,
  topic VARCHAR(40) NOT NULL,
  difficulty VARCHAR(24) NOT NULL,
  prompt TEXT NOT NULL,
  model_answer TEXT NOT NULL,
  rubric TEXT NOT NULL
);
CREATE INDEX idx_interview_questions_topic_difficulty ON interview_questions (topic,difficulty);

CREATE TABLE interview_sessions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  role VARCHAR(160) NOT NULL,
  topic VARCHAR(40) NOT NULL,
  difficulty VARCHAR(24) NOT NULL,
  state VARCHAR(24) NOT NULL,
  started_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  ended_at TIMESTAMPTZ,
  average_score INTEGER
);
CREATE INDEX idx_interview_sessions_owner_date ON interview_sessions (user_id,started_at DESC);

CREATE TABLE interview_answers (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  session_id UUID NOT NULL REFERENCES interview_sessions(id) ON DELETE CASCADE,
  question_id UUID NOT NULL REFERENCES interview_questions(id),
  ordinal INTEGER NOT NULL,
  answer TEXT NOT NULL,
  content_coverage_score INTEGER NOT NULL CHECK (content_coverage_score BETWEEN 0 AND 100),
  feedback TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_interview_answer_ordinal UNIQUE (session_id,ordinal),
  CONSTRAINT uq_interview_answer_question UNIQUE (session_id,question_id)
);
CREATE INDEX idx_interview_answers_session ON interview_answers (session_id,ordinal);

INSERT INTO interview_questions (seed_key,topic,difficulty,prompt,model_answer,rubric) VALUES
('java-oop-encapsulation','JAVA','BEGINNER','What is encapsulation in Java, and how do access modifiers support it?','Encapsulation bundles state and behavior while controlling access to implementation details. Private fields with validated public methods protect invariants; package, protected, and public visibility define the intended boundary.','Look for definition, controlled access, and a concrete invariant or example.'),
('java-equals-hashcode','JAVA','INTERMEDIATE','What contract should equals and hashCode follow when objects are used as HashMap keys?','Equal objects must return the same hash code. hashCode must remain stable while equality-relevant state is unchanged. HashMap uses the hash to choose a bucket and equals to identify a matching key.','Look for equal-implies-same-hash and the role of both methods.'),
('spring-di','SPRING_BOOT','BEGINNER','What is dependency injection, and why is constructor injection usually preferred in Spring?','Dependency injection supplies a component its collaborators instead of constructing them internally. Constructor injection makes required dependencies explicit, supports immutability, and simplifies unit tests.','Look for inversion of control, explicit dependencies, and testability.'),
('spring-transactions','SPRING_BOOT','ADVANCED','How does Spring transaction propagation affect a service method called inside another transaction?','Propagation defines whether a method joins, suspends, or creates a transaction. REQUIRED joins or creates one; REQUIRES_NEW suspends the current transaction and starts another. Proxy boundaries and rollback rules matter.','Look for REQUIRED and REQUIRES_NEW semantics and proxy/rollback caveats.'),
('sql-index','SQL','BEGINNER','What does a database index do, and what is one tradeoff of adding an index?','An index helps the database locate rows without scanning every row. It uses storage and adds work to inserts, updates, and deletes, so indexes should support measured query patterns.','Look for lookup benefit and write/storage cost.'),
('sql-joins','SQL','INTERMEDIATE','Explain the difference between an INNER JOIN and a LEFT JOIN.','INNER JOIN returns rows with matches on both sides. LEFT JOIN preserves every left-side row and fills right-side columns with null when no match exists.','Look for preservation of unmatched left rows.'),
('dbms-acid','DBMS','INTERMEDIATE','Describe the ACID properties of a database transaction.','Atomicity groups operations as all-or-nothing; consistency preserves constraints; isolation limits interference among concurrent transactions; durability keeps committed work after failure.','Look for all four properties with accurate meanings.'),
('behavioral-star','BEHAVIORAL','BEGINNER','Tell me about a difficult problem you solved. How would you structure your answer?','A clear response gives the situation and task, describes personal actions and reasoning, and finishes with a result and a reflection. A specific, truthful example is more useful than a general claim.','Look for a specific situation, the candidate’s actions, an outcome, and a reflection.');
