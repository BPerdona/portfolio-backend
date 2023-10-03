<h1 align="center">
  💾<br>Portfólio - Backend
</h1>

## 👔 Aprensentação
Olá, esse é o Backend do meu Portfólio. Nele será possivel ver todas as dependências, rotas, estrutura e claro as 
implementações necessárias que tive que fazer para deixar tudo funcional e seguro.

Espero que goste e caso tenha alguma dúvida basta entrar em contato!

## 🛠️ Instalação
Para realizar a devida instação do projeto você devera instalar:
- Java SDK 17
- Docker
- Instalar dependências do Maven
- IntelliJ (Recomendação)

Apos rodar a imagem do banco PSQL com o docker, será necessário criar um banco com o nome spring. Passos:
- Entrar no docker: ``docker exec -it postgres bash``
- Entrar no PSQL: ``psql -U user``
- Criar banco: ``CREATE DATABASE spring;``
- Aperte CTRL+D duas vezes para sair
- Agora só basta rodar o projeto =D

## 📚 Dependencias e Tecnologias

- Spring Web
- Spring Security
- Spring Validation
- JPA
- Hateoas
- Postgresql
- Docker

## 📝 Features

- Autenticação e Autorização utilizando JWT
- Docker do banco PSQL

## 🛣️ Rotas

### ➡️ Rota base: ``/api/v1/``

### Autenticação JWT

- POST ``auth/register``
  - Registro de usuário
  - Não é permitido emails repitidos
  - Dados necessários: nome, email e senha
  - Dados de retorno: accessToken e refreshToken
- POST ``auth/authenticate``
  - Autenticação de usuário
  - Dados necessários: email e senha
  - Dados de retorno: accessToken e refreshToken
- POST ``auth/refresh-token``
  - Refresh do accessToken
  - Necessário adicionar o refreshToken no Bearer da requisição
  - Dados de retorno: accessToken e refreshToken

> O tempo de validade de cada token podem ser alterados em ``resources/application.yml`` no parâmetro expiration.

> Certas rotas precisaram de autenticação e/ou autorização

#### **🚧 Em Construção 🚧**