CREATE TABLE pets (
    id SERIAL PRIMARY KEY,
    data_nascimento TIMESTAMP,
    nome VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    cliente_id INTEGER NOT NULL REFERENCES clientes(id),
    raca_id INTEGER NOT NULL REFERENCES racas(id)
);

CREATE INDEX idx_pets_deleted ON pets(deleted);