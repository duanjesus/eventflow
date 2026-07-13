CREATE TABLE institution_mapping (
    id                 BIGSERIAL PRIMARY KEY,
    institution_id     BIGINT NOT NULL,
    pulsehub_user_id   BIGINT NOT NULL,
    created_at         TIMESTAMP NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX idx_institution_mapping_institution_id ON institution_mapping (institution_id);
