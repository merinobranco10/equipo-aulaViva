-- AulaViva S18 - Esquema inicial (alineado a docs/data/der.dbml).
-- Todas las tablas de negocio llevan tenant_id (1 colegio = 1 tenant), directo o via su agregado.

CREATE TABLE tenants (
  id   VARCHAR(36)  PRIMARY KEY,
  name VARCHAR(120) NOT NULL
);

CREATE TABLE users (
  id            VARCHAR(36)  PRIMARY KEY,
  tenant_id     VARCHAR(36)  NOT NULL REFERENCES tenants(id),
  username      VARCHAR(60)  NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  full_name     VARCHAR(120) NOT NULL,
  role          VARCHAR(20)  NOT NULL -- STUDENT | TEACHER | COORDINATOR | GUARDIAN
);

CREATE TABLE student_guardians (
  guardian_id VARCHAR(36) NOT NULL REFERENCES users(id),
  student_id  VARCHAR(36) NOT NULL REFERENCES users(id),
  PRIMARY KEY (guardian_id, student_id)
);

CREATE TABLE courses (
  id         VARCHAR(36)  PRIMARY KEY,
  tenant_id  VARCHAR(36)  NOT NULL REFERENCES tenants(id),
  name       VARCHAR(120) NOT NULL,
  teacher_id VARCHAR(36)  REFERENCES users(id)
);

CREATE TABLE enrollments (
  course_id  VARCHAR(36) NOT NULL REFERENCES courses(id),
  student_id VARCHAR(36) NOT NULL REFERENCES users(id),
  PRIMARY KEY (course_id, student_id)
);

CREATE TABLE evaluations (
  id              VARCHAR(36)  PRIMARY KEY,
  tenant_id       VARCHAR(36)  NOT NULL REFERENCES tenants(id),
  course_id       VARCHAR(36)  NOT NULL REFERENCES courses(id),
  title           VARCHAR(200) NOT NULL,
  status          VARCHAR(20)  NOT NULL,
  created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
  idempotency_key VARCHAR(100),
  UNIQUE (tenant_id, idempotency_key)
);

CREATE TABLE questions (
  id            VARCHAR(36)   PRIMARY KEY,
  evaluation_id VARCHAR(36)   NOT NULL REFERENCES evaluations(id),
  text          VARCHAR(1000) NOT NULL,
  points        INT           NOT NULL,
  position      INT           NOT NULL,
  explanation   VARCHAR(1000),
  UNIQUE (evaluation_id, position)
);

CREATE TABLE options (
  id          VARCHAR(36)  PRIMARY KEY,
  question_id VARCHAR(36)  NOT NULL REFERENCES questions(id),
  text        VARCHAR(500) NOT NULL,
  position    INT          NOT NULL,
  is_correct  BOOLEAN      NOT NULL,
  UNIQUE (question_id, position)
);

CREATE TABLE submissions (
  id              VARCHAR(36) PRIMARY KEY,
  tenant_id       VARCHAR(36) NOT NULL REFERENCES tenants(id),
  evaluation_id   VARCHAR(36) NOT NULL REFERENCES evaluations(id),
  student_id      VARCHAR(36) NOT NULL REFERENCES users(id),
  submitted_at    TIMESTAMP WITH TIME ZONE,
  status          VARCHAR(20) NOT NULL,
  idempotency_key VARCHAR(100),
  UNIQUE (evaluation_id, student_id)
);

CREATE TABLE answers (
  id            VARCHAR(36) PRIMARY KEY,
  submission_id VARCHAR(36) NOT NULL REFERENCES submissions(id),
  question_id   VARCHAR(36) NOT NULL REFERENCES questions(id),
  option_id     VARCHAR(36) REFERENCES options(id),
  UNIQUE (submission_id, question_id)
);

CREATE TABLE grades (
  id             VARCHAR(36)   PRIMARY KEY,
  submission_id  VARCHAR(36)   NOT NULL UNIQUE REFERENCES submissions(id),
  grade          DECIMAL(2,1)  NOT NULL,
  score_obtained INT           NOT NULL,
  max_score      INT           NOT NULL,
  feedback       VARCHAR(4000)
);

CREATE TABLE outbox_events (
  id         VARCHAR(36)   PRIMARY KEY,
  tenant_id  VARCHAR(36)   NOT NULL REFERENCES tenants(id),
  event_type VARCHAR(60)   NOT NULL,
  payload    VARCHAR(2000) NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL,
  published  BOOLEAN       NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_users_tenant        ON users(tenant_id);
CREATE INDEX idx_courses_tenant      ON courses(tenant_id);
CREATE INDEX idx_evaluations_tenant  ON evaluations(tenant_id, course_id);
CREATE INDEX idx_submissions_student ON submissions(student_id, tenant_id);
CREATE INDEX idx_outbox_pending      ON outbox_events(published, created_at);
