CREATE TABLE feature_flags
(
    id         integer PRIMARY KEY CHECK (id = 1),
    state      varchar(20) NOT NULL CHECK (state IN (
                                                     'StartV2',
                                                     'BackFillV2',
                                                     'ActivatedV2AndSync',
                                                     'ActivatedV2',
                                                     'ActivatedV1'
        )),
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    version    bigint      NOT NULL DEFAULT 0
);

INSERT INTO feature_flags (id, state, created_at, updated_at)
VALUES (1, 'ActivatedV1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
