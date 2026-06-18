# Projeto Web 3 - Microsservicos com JWT, RabbitMQ, Email e Frontend

## Visao geral
Este projeto implementa um fluxo completo de autenticacao por codigo (OTP) com arquitetura de microsservicos.

Componentes principais:
- User Service (Spring Boot): cadastro/autenticacao JWT, geracao e validacao do codigo OTP, atualizacao de perfil e endpoints protegidos.
- Email Service (Spring Boot): consumo da fila RabbitMQ, envio de email real via Gmail SMTP e persistencia dos envios.
- Frontend (Node.js + Express): fluxo web de solicitacao de codigo, validacao, cadastro de perfil e dashboard protegido.

## Arquitetura
- User Service publica mensagens na fila default.email.
- Email Service consome a fila default.email e envia o email real.
- Frontend chama o User Service para:
	- solicitar codigo;
	- validar codigo;
	- atualizar perfil;
	- consultar perfil e endpoint protegido.

Fluxo resumido:
1. Usuario informa email no frontend.
2. User Service gera OTP e publica na fila.
3. Email Service consome e envia email.
4. Usuario valida codigo no frontend.
5. Frontend recebe JWT e segue para cadastro de nome/cargo.
6. Dashboard usa o JWT para chamadas protegidas.

## Estrutura de pastas
- entrega1/user-service: codigo fonte do User Service
- entrega1/email-service: codigo fonte do Email Service
- entrega1/frontend: codigo fonte do Frontend
- entrega1/evidencias_entrega1: evidencias da etapa 1
- entrega1/evidencias_entrega2: evidencias da etapa 2
- entrega1/entrega3/prints: evidencias do email recebido e fluxo
- entrega4: pacote final com os tres servicos (codigo fonte completo)

## Pre-requisitos
- Java 17+
- Maven 3.9+
- Node.js 18+
- MySQL 8+
- Conta CloudAMQP
- Conta Gmail com senha de aplicativo

## Banco de dados
Execute no MySQL:

~~~sql
CREATE DATABASE ms_user;
CREATE DATABASE ms_email;
~~~

## Configuracao

### 1) User Service
Arquivo: entrega1/user-service/src/main/resources/application.properties

Campos importantes:
- server.port=8081
- spring.datasource.url=jdbc:mysql://localhost:3306/ms_user?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
- spring.datasource.username=root
- spring.datasource.password=${DB_PASSWORD:REPLACE_WITH_YOUR_PASSWORD}
- spring.jpa.hibernate.ddl-auto=update
- spring.rabbitmq.addresses=<URI_CLOUDAMQP>
- broker.queue.email.name=default.email

### 2) Email Service
Arquivo: entrega1/email-service/src/main/resources/application.properties

Campos importantes:
- server.port=8082
- spring.datasource.url=jdbc:mysql://localhost:3306/ms_email?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
- spring.datasource.username=root
- spring.datasource.password=${DB_PASSWORD:REPLACE_WITH_YOUR_PASSWORD}
- spring.jpa.hibernate.ddl-auto=update
- spring.rabbitmq.addresses=<URI_CLOUDAMQP>
- broker.queue.email.name=default.email
- spring.mail.host=smtp.gmail.com
- spring.mail.port=587
- spring.mail.username=<SEU_EMAIL_GMAIL>
- spring.mail.password=<SUA_SENHA_APP>
- spring.mail.properties.mail.smtp.auth=true
- spring.mail.properties.mail.smtp.starttls.enable=true

### 3) Frontend
Arquivo: entrega1/frontend/server.js

Padrao:
- USER_SERVICE_URL=http://localhost:8081
- frontend em http://localhost:3000

## Como executar manualmente

### Terminal 1 - User Service
~~~powershell
cd entrega1/user-service
$env:DB_PASSWORD="SUA_SENHA_MYSQL"
mvn spring-boot:run
~~~

### Terminal 2 - Email Service
~~~powershell
cd entrega1/email-service
$env:DB_PASSWORD="SUA_SENHA_MYSQL"
mvn spring-boot:run
~~~

### Terminal 3 - Frontend
~~~powershell
cd entrega1/frontend
npm install
npm start
~~~

## Script de inicializacao
Existe script para Windows na raiz do projeto:
- iniciar.ps1

O script:
- solicita a senha do MySQL para preencher `DB_PASSWORD`;
- abre janelas separadas para User Service, Email Service e Frontend;
- executa `npm install` automaticamente no frontend antes do `npm start`.

Execucao (na raiz do projeto):
~~~powershell
cd .
./iniciar.ps1
~~~

## Endpoints principais

### User Service
- POST /users
- POST /users/login
- GET /users/test/customer
- POST /auth/request-code
- POST /auth/verify-code
- POST /users/update-profile
- GET /users/me

### Frontend
- GET /
- POST /send-code
- GET /verify
- POST /verify-code
- GET /register
- POST /register
- GET /dashboard
- GET /api/protected
- GET /users/me (proxy)

## Fluxo funcional (etapa 4)
1. Acessar http://localhost:3000
2. Informar email na pagina inicial.
3. Receber codigo no email.
4. Validar codigo na pagina verify.
5. Preencher nome e cargo na pagina register.
6. Acessar dashboard e testar:
	 - endpoint protegido;
	 - consulta de perfil;
	 - logout.


