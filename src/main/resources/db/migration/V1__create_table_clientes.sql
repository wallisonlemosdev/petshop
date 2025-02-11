-- Tabela: Clientes
CREATE TABLE clientes (
      id SERIAL PRIMARY KEY,
      cpf VARCHAR(11) NOT NULL UNIQUE,
      nome VARCHAR(255) NOT NULL,
      created_at TIMESTAMP NOT NULL DEFAULT NOW(),
      updated_at TIMESTAMP,
      deleted_at TIMESTAMP,
      deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_clientes_deleted ON clientes(deleted);