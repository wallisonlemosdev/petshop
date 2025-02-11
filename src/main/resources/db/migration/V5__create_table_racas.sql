CREATE TABLE racas (
    id SERIAL PRIMARY KEY,
    descricao VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_racas_deleted ON racas(deleted);
