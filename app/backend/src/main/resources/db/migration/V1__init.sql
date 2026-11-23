CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    full_name     VARCHAR(100)  NOT NULL,
    email         VARCHAR(255)  NOT NULL UNIQUE,
    password_hash VARCHAR(100)  NOT NULL,
    role          VARCHAR(20)   NOT NULL,
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE TABLE diary_entries (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    title       VARCHAR(200)  NOT NULL,
    content     TEXT          NOT NULL,
    mood        VARCHAR(20)   NOT NULL,
    tags        VARCHAR(500),
    entry_date  DATE          NOT NULL,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_diary_entries_user_date ON diary_entries (user_id, entry_date DESC);
