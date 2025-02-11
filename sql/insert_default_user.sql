INSERT INTO clientes (cpf, nome, created_at, updated_at, deleted)
VALUES
    ('07177804064', 'Administrador', NOW(), NOW(), FALSE);

INSERT INTO enderecos (logradouro, cidade, bairro, complemento, tag, cliente_id, created_at, updated_at, deleted)
VALUES
    ('Rua Admin', 'Cidade Admin', 'Bairro Admin', 'Complemento Admin', 'Residencial',
     (SELECT id FROM clientes WHERE cpf = '07177804064'), NOW(), NOW(), FALSE);

INSERT INTO contatos (tag, tipo, valor, cliente_id, created_at, updated_at, deleted)
VALUES
    ('Pessoal', 'TELEFONE', '(87)99999-9999',
     (SELECT id FROM clientes WHERE cpf = '07177804064'), NOW(), NOW(), FALSE);

INSERT INTO usuarios (cpf, nome, perfil, senha, cliente_id, created_at, updated_at, deleted)
VALUES
    ('07177804064', 'Administrador', 'ADMIN',
     '$2a$10$SICKT7cXgwlU3DPTp/ckgeRend2MtVC9Igh3m.k0hiUqAfRtsYVdu',
     (SELECT id FROM clientes WHERE cpf = '07177804064'), NOW(), NOW(), FALSE);
