CREATE TABLE users_v2
(
    id         uuid PRIMARY KEY,
    username   varchar(40)  NOT NULL,
    name       varchar(100) NOT NULL,
    surname    varchar(100) NOT NULL,
    created_at timestamptz  NOT NULL,
    updated_at timestamptz  NOT NULL,
    version    bigint       NOT NULL DEFAULT 0,
    CONSTRAINT uk_users_v2_username UNIQUE (username)
);

CREATE INDEX idx_users_v2_created_at ON users_v2 (created_at);
CREATE INDEX idx_users_v2_updated_at ON users_v2 (updated_at);
