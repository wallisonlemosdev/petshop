## Descrição do Projeto
O **Petshop** é um sistema simples de controle de atendimentos para uma petshop, proposto em um desafio técnico, desenvolvido em Java utilizando o framework Spring Boot. O sistema permite gerenciar clientes, seus pets, atendimentos, raças e usuários com dois perfis distintos:

- **Admin:** Permite incluir, excluir, alterar e visualizar qualquer cadastro.
- **Cliente:** Pode visualizar e alterar apenas seus registros e/ou registros dos seus pets.

## Requisitos Atendidos
- Banco de dados relacional (PostgreSQL).
- JDK 17.
- Testes unitários para validar o funcionamento da API.
- Autenticação por token JWT.
- Autorizacão baseada em roles (Admin e Cliente).
- Migração de banco de dados com Flyway.

## Configuração do Ambiente
1. Certifique-se de ter o Java 17 ou superior instalado.
2. Instale o Docker.
3. Clone o repositório do projeto:
   ```bash
   git clone https://github.com/wallisonlemosdev/petshop.git
   cd petshop
   ```

### Configuração do Banco de Dados
O projeto utiliza PostgreSQL com Docker Compose. Para iniciar os containers do banco de dados de produção e testes vá até o diretório /docker dentro do projeto e execute o comando:

```bash
docker-compose up -d
```

O script `init-db-test.sh` cria automaticamente o banco de dados de testes `petshopdbtest`.

## Execução da Aplicação
Para executar a aplicação localmente via IDE (De sua preferência) ou:

```bash
mvn clean spring-boot:run
```
A aplicação será iniciada em [http://localhost:8080](http://localhost:8080).

## Testes de Integração
Para rodar os testes de integração:

```bash
mvn clean verify
```
Os testes usam o banco de dados `petshopdbtest` configurado via Docker Compose.

## Migrações de Banco de Dados
As tabelas são criadas através de migrações definidas com Flyway. Certifique-se de que as migrações estejam no diretório `src/main/resources/db/migration`.

## Autenticação e Autorizacão
A autenticação é feita através de JWT (JSON Web Tokens). Para acessar os endpoints protegidos:

1. Obtenha um token de autenticação fazendo login no endpoint de autenticação.
2. Inclua o token no cabeçalho `Authorization` das requisições HTTP:

   ```
   Authorization: Bearer <token>
   ```

## Estrutura de Pastas
- **src/main/java:** Contém o código-fonte da aplicação.
- **src/test/java:** Contém os testes unitários e de integração.
- **src/main/resources/db/migration:** Scripts de migração Flyway.

## Contato

📧 E-mail: w.alexandrelemos@gmail.com

🔗 LinkedIn: https://www.linkedin.com/in/wallisonlemosdev/

📱 WhatsApp: https://wa.me/5587981003268
