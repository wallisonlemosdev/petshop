CREATE TABLE contatos (
    id SERIAL PRIMARY KEY,
    tag VARCHAR(50),
    tipo VARCHAR(50) NOT NULL,
    valor VARCHAR(255) NOT NULL,
    cliente_id INTEGER NOT NULL UNIQUE REFERENCES clientes(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_contatos_deleted ON contatos(deleted);
