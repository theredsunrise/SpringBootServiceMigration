CREATE TABLE users_v1
(
    id         uuid         NOT NULL,
    username   varchar(40)  NOT NULL,
    name       varchar(100) NOT NULL,
    created_at timestamptz  NOT NULL,
    updated_at timestamptz  NOT NULL,
    version    bigint       NOT NULL DEFAULT 0,
    CONSTRAINT pk_users_v1 PRIMARY KEY (id),
    CONSTRAINT uk_users_v1_username UNIQUE (username)
);

CREATE INDEX idx_users_v1_created_at ON users_v1 (created_at);
CREATE INDEX idx_users_v1_updated_at ON users_v1 (updated_at);
