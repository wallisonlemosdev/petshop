CREATE TABLE atendimentos (
    id SERIAL PRIMARY KEY,
    descricao VARCHAR(255) NOT NULL,
    valor NUMERIC(10, 2),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    pet_id INTEGER NOT NULL REFERENCES pets(id)
);

CREATE INDEX idx_atendimentos_deleted ON atendimentos(deleted);