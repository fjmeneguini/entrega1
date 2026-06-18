# Projeto Web 3 - Microsserviços com JWT, RabbitMQ e Frontend

## Arquitetura
- `user-service` (Spring Boot): autenticação JWT, geração e validação de código OTP, atualização de perfil e endpoints protegidos.
- `email-service` (Spring Boot): consumidor da fila RabbitMQ, envio de e-mail real via Gmail SMTP e persistência dos envios no MySQL.
- `frontend` (Node.js + Express): páginas web do fluxo (`/`, `/verify`, `/register`, `/dashboard`) e rotas proxy para o `user-service`.
- Mensageria: CloudAMQP com fila `default.email`.
- Bancos MySQL:
  - `ms_user`
  - `ms_email`

## Pré-requisitos
- JDK 17+
- Maven 3.9+
- Node.js 18+
- MySQL 8+
- Conta CloudAMQP
- Conta Gmail com senha de aplicativo

## Configuração

### 1. Criar bancos
```sql
CREATE DATABASE ms_user;
CREATE DATABASE ms_email;
```

### 2. Variáveis e propriedades
- Defina `DB_PASSWORD` no terminal antes de iniciar os serviços Java.
- Configure os arquivos:
  - `user-service/src/main/resources/application.properties`
  - `email-service/src/main/resources/application.properties`

Pontos obrigatórios:
- `spring.rabbitmq.addresses` com a URI do CloudAMQP.
- `broker.queue.email.name=default.email`.
- SMTP Gmail no `email-service`:
  - `spring.mail.host=smtp.gmail.com`
  - `spring.mail.port=587`
  - `spring.mail.username=...`
  - `spring.mail.password=...`

## Como executar

### Terminal 1 - User Service
```powershell
cd user-service
$env:DB_PASSWORD="SUA_SENHA_MYSQL"
mvn spring-boot:run
```

### Terminal 2 - Email Service
```powershell
cd email-service
$env:DB_PASSWORD="SUA_SENHA_MYSQL"
mvn spring-boot:run
```

### Terminal 3 - Frontend
```powershell
cd frontend
npm install
npm start
```

A aplicação web ficará em `http://localhost:3000`.

## Script de inicialização (Windows)
- Arquivo: `iniciar.ps1`
- O script abre 3 terminais separados para `user-service`, `email-service` e `frontend`.
- Solicita a senha do MySQL para preencher `DB_PASSWORD` nos serviços Java.

Execução:
```powershell
cd .
./iniciar.ps1
```

## Fluxo completo
1. Abrir `http://localhost:3000`.
2. Informar e-mail na página inicial.
3. Receber código por e-mail.
4. Validar código na tela `/verify`.
5. Completar perfil em `/register` (nome e cargo).
6. Usar o dashboard para:
   - testar endpoint protegido;
   - consultar perfil;
   - sair.

## Capturas de tela
- Evidências da etapa 1: `evidencias_entrega1/`
- Evidências da etapa 2: `evidencias_entrega2/`
- Evidências da etapa 3/4 (fluxo e e-mail): `entrega3/prints/`

## Versionamento das etapas
- `entrega1`
- `entrega2`
- `entrega3`
- `entrega4`

## Arquivo de ignore
- O projeto inclui `.gitignore` na raiz do repositório (`entrega1/.gitignore`) com regras para `target/`, `node_modules/` e artefatos locais.
