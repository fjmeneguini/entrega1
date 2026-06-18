# Entrega 3


## Email Service

### `EmailModel.java`
```java
package com.ifsp.emailservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "emails")
public class EmailModel {

		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		private Long emailId;

		@Column(nullable = false)
		private UUID userId;

		@Column(nullable = false)
		private String emailFrom;

		@Column(nullable = false)
		private String emailTo;

		@Column(nullable = false)
		private String subject;

		@Column(nullable = false, columnDefinition = "TEXT")
		private String text;

		@Column(nullable = false)
		private LocalDateTime sendDateEmail;

		@Enumerated(EnumType.STRING)
		@Column(nullable = false)
		private EmailStatus status;

		@PrePersist
		void onCreate() {
				if (sendDateEmail == null) {
						sendDateEmail = LocalDateTime.now();
				}
		}

		public Long getEmailId() {
				return emailId;
		}

		public void setEmailId(Long emailId) {
				this.emailId = emailId;
		}

		public UUID getUserId() {
				return userId;
		}

		public void setUserId(UUID userId) {
				this.userId = userId;
		}

		public String getEmailFrom() {
				return emailFrom;
		}

		public void setEmailFrom(String emailFrom) {
				this.emailFrom = emailFrom;
		}

		public String getEmailTo() {
				return emailTo;
		}

		public void setEmailTo(String emailTo) {
				this.emailTo = emailTo;
		}

		public String getSubject() {
				return subject;
		}

		public void setSubject(String subject) {
				this.subject = subject;
		}

		public String getText() {
				return text;
		}

		public void setText(String text) {
				this.text = text;
		}

		public LocalDateTime getSendDateEmail() {
				return sendDateEmail;
		}

		public void setSendDateEmail(LocalDateTime sendDateEmail) {
				this.sendDateEmail = sendDateEmail;
		}

		public EmailStatus getStatus() {
				return status;
		}

		public void setStatus(EmailStatus status) {
				this.status = status;
		}
}
```

### `EmailStatus.java`
```java
package com.ifsp.emailservice.model;

public enum EmailStatus {
		SENT,
		ERROR
}
```

### `EmailRecordDto.java`
```java
package com.ifsp.emailservice.dto;

import java.util.UUID;

public class EmailRecordDto {

		private UUID userId;
		private String emailTo;
		private String subject;
		private String text;

		public EmailRecordDto() {
		}

		public EmailRecordDto(UUID userId, String emailTo, String subject, String text) {
				this.userId = userId;
				this.emailTo = emailTo;
				this.subject = subject;
				this.text = text;
		}

		public UUID getUserId() {
				return userId;
		}

		public void setUserId(UUID userId) {
				this.userId = userId;
		}

		public String getEmailTo() {
				return emailTo;
		}

		public void setEmailTo(String emailTo) {
				this.emailTo = emailTo;
		}

		public String getSubject() {
				return subject;
		}

		public void setSubject(String subject) {
				this.subject = subject;
		}

		public String getText() {
				return text;
		}

		public void setText(String text) {
				this.text = text;
		}
}
```

### `EmailRepository.java`
```java
package com.ifsp.emailservice.repository;

import com.ifsp.emailservice.model.EmailModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailRepository extends JpaRepository<EmailModel, Long> {
}
```

### `RabbitMQConfig.java`
```java
package com.ifsp.emailservice.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

		@Bean
		public Queue emailQueue() {
				return new Queue("default.email", true);
		}

		@Bean
		public MessageConverter jackson2JsonMessageConverter() {
				return new Jackson2JsonMessageConverter();
		}
}
```

### `EmailService.java`
```java
package com.ifsp.emailservice.service;

import com.ifsp.emailservice.dto.EmailRecordDto;
import com.ifsp.emailservice.model.EmailModel;
import com.ifsp.emailservice.model.EmailStatus;
import com.ifsp.emailservice.repository.EmailRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EmailService {

		private final EmailRepository emailRepository;
		private final JavaMailSender mailSender;
		private final String emailFrom;

		public EmailService(EmailRepository emailRepository,
												JavaMailSender mailSender,
												@Value("${spring.mail.username}") String emailFrom) {
				this.emailRepository = emailRepository;
				this.mailSender = mailSender;
				this.emailFrom = emailFrom;
		}

		public EmailModel sendEmail(EmailRecordDto emailRecordDto) {
				EmailModel emailModel = new EmailModel();
				emailModel.setUserId(emailRecordDto.getUserId());
				emailModel.setEmailFrom(emailFrom);
				emailModel.setEmailTo(emailRecordDto.getEmailTo());
				emailModel.setSubject(emailRecordDto.getSubject());
				emailModel.setText(emailRecordDto.getText());
				emailModel.setSendDateEmail(LocalDateTime.now());

				try {
						SimpleMailMessage message = new SimpleMailMessage();
						message.setFrom(emailFrom);
						message.setTo(emailRecordDto.getEmailTo());
						message.setSubject(emailRecordDto.getSubject());
						message.setText(emailRecordDto.getText());
						mailSender.send(message);
						emailModel.setStatus(EmailStatus.SENT);
				} catch (Exception ex) {
						emailModel.setStatus(EmailStatus.ERROR);
				}

				return emailRepository.save(emailModel);
		}
}
```

### `EmailConsumer.java`
```java
package com.ifsp.emailservice.consumer;

import com.ifsp.emailservice.dto.EmailRecordDto;
import com.ifsp.emailservice.service.EmailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class EmailConsumer {

		private final EmailService emailService;

		public EmailConsumer(EmailService emailService) {
				this.emailService = emailService;
		}

		@RabbitListener(queues = "${broker.queue.email.name}")
		public void receive(EmailRecordDto emailRecordDto) {
				emailService.sendEmail(emailRecordDto);
		}
}
```

## Frontend

### `server.js`
```javascript
const express = require('express');
const axios = require('axios');
const path = require('path');

const app = express();
const userServiceApi = axios.create({
	baseURL: process.env.USER_SERVICE_URL || 'http://localhost:8081',
	timeout: 10000
});

app.use(express.urlencoded({ extended: true }));
app.use(express.json());
app.use('/vendor/axios', express.static(path.join(__dirname, 'node_modules', 'axios', 'dist')));
app.use('/static', express.static(path.join(__dirname, 'public')));

app.get('/', function (req, res) {
	res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

app.post('/send-code', function (req, res) {
	var email = (req.body.email || '').trim();
	if (!email) {
		return res.status(400).send('E-mail obrigatorio.');
	}

	userServiceApi.post('/auth/request-code', { email: email })
		.then(function () {
			res.redirect('/verify?email=' + encodeURIComponent(email));
		})
		.catch(function (error) {
			var message = (error.response && error.response.data && error.response.data.error) || 'Nao foi possivel solicitar o codigo.';
			res.status(500).send(message);
		});
});

app.get('/verify', function (req, res) {
	res.sendFile(path.join(__dirname, 'public', 'verify.html'));
});

app.post('/verify-code', function (req, res) {
	var email = (req.body.email || '').trim();
	var code = (req.body.code || '').trim();
	if (!email || !code) {
		return res.status(400).json({ error: 'E-mail e codigo sao obrigatorios.' });
	}

	userServiceApi.post('/auth/verify-code', { email: email, code: code })
		.then(function (response) {
			res.json({
				message: response.data.message,
				token: response.data.token
			});
		})
		.catch(function (error) {
			var status = (error.response && error.response.status) || 500;
			var message = (error.response && error.response.data && error.response.data.error) || 'Codigo invalido ou expirado.';
			res.status(status).json({ error: message });
		});
});

app.get('/dashboard', function (req, res) {
	res.sendFile(path.join(__dirname, 'public', 'dashboard.html'));
});

app.listen(3000, function () {
	console.log('Frontend running on http://localhost:3000');
});
```

### `public/index.html`
```html
<!DOCTYPE html>
<html lang="pt-br">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<title>Solicitar código</title>
	<link rel="stylesheet" href="/static/styles.css">
</head>
<body>
	<main class="page">
		<section class="card">
			<h1 class="title">Solicitar código</h1>
			<p class="subtitle">Digite seu e-mail para receber o código de acesso.</p>
			<form action="/send-code" method="post">
				<div class="field">
					<label for="email">E-mail</label>
					<input id="email" name="email" type="email" placeholder="seuemail@exemplo.com" required>
				</div>
				<div class="actions">
					<button class="button primary" type="submit">Enviar código</button>
				</div>
			</form>
			<p class="hint">O código será enviado por e-mail e depois validado na próxima tela.</p>
		</section>
	</main>
</body>
</html>
```

### `public/verify.html`
```html
<!DOCTYPE html>
<html lang="pt-br">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<title>Validar código</title>
	<link rel="stylesheet" href="/static/styles.css">
	<script src="/vendor/axios/axios.min.js"></script>
</head>
<body>
	<main class="page">
		<section class="card">
			<h1 class="title">Validar código</h1>
			<p class="subtitle">Confirme o e-mail e digite o código recebido.</p>
			<form id="verify-form">
				<div class="field">
					<label for="email">E-mail</label>
					<input id="email" name="email" type="email" required>
				</div>
				<div class="field">
					<label for="code">Código</label>
					<input id="code" name="code" type="text" maxlength="6" placeholder="000000" required>
				</div>
				<div class="actions">
					<button class="button primary" type="submit">Validar</button>
					<a class="button secondary" href="/">Voltar</a>
				</div>
			</form>
			<div id="message" class="message"></div>
		</section>
	</main>

	<script>
		(function () {
			var params = new URLSearchParams(window.location.search);
			var emailInput = document.getElementById('email');
			var codeInput = document.getElementById('code');
			var messageBox = document.getElementById('message');
			var form = document.getElementById('verify-form');

			if (params.get('email')) {
				emailInput.value = params.get('email');
			}

			function setMessage(text, isError) {
				messageBox.className = isError ? 'message error' : 'message success';
				messageBox.textContent = text;
			}

			form.addEventListener('submit', function (event) {
				event.preventDefault();

				axios.post('/verify-code', {
					email: emailInput.value.trim(),
					code: codeInput.value.trim()
				}).then(function (response) {
					if (response.data && response.data.token) {
						sessionStorage.setItem('token', response.data.token);
						setMessage('Código validado. Redirecionando...', false);
						window.location.href = '/dashboard';
						return;
					}

					setMessage('Resposta inválida do servidor.', true);
				}).catch(function (error) {
					var fallback = 'Código inválido ou expirado.';
					var serverMessage = error && error.response && error.response.data && error.response.data.error;
					setMessage(serverMessage || fallback, true);
				});
			});
		}());
	</script>
</body>
</html>
```

### `public/dashboard.html`
```html
<!DOCTYPE html>
<html lang="pt-br">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<title>Painel</title>
	<link rel="stylesheet" href="/static/styles.css">
</head>
<body>
	<main class="page">
		<section class="card">
			<h1 class="title">Acesso liberado</h1>
			<p class="subtitle">O código foi validado e a sessão foi criada.</p>
			<div id="token-box" class="token-box">Carregando token...</div>
			<div class="actions" style="margin-top: 20px;">
				<a class="button secondary" href="/">Nova solicitação</a>
			</div>
		</section>
	</main>

	<script>
		(function () {
			var token = sessionStorage.getItem('token');
			var box = document.getElementById('token-box');

			if (!token) {
				box.textContent = 'Nenhum token encontrado na sessão.';
				return;
			}

			box.textContent = 'Token JWT: ' + token;
		}());
	</script>
</body>
</html>
```

### `public/styles.css`
```css
:root {
	--bg: #f4f1ea;
	--panel: #ffffff;
	--text: #1f2937;
	--muted: #6b7280;
	--accent: #b45309;
	--accent-dark: #92400e;
	--border: #e5e7eb;
	--shadow: 0 18px 50px rgba(17, 24, 39, 0.08);
}

* {
	box-sizing: border-box;
}

body {
	margin: 0;
	min-height: 100vh;
	font-family: Arial, Helvetica, sans-serif;
	color: var(--text);
	background:
		radial-gradient(circle at top left, rgba(180, 83, 9, 0.12), transparent 30%),
		linear-gradient(180deg, #fffdf8 0%, var(--bg) 100%);
}

.page {
	min-height: 100vh;
	display: grid;
	place-items: center;
	padding: 24px;
}

.card {
	width: 100%;
	max-width: 460px;
	background: var(--panel);
	border: 1px solid var(--border);
	border-radius: 22px;
	padding: 32px;
	box-shadow: var(--shadow);
}

.title {
	margin: 0 0 8px;
	font-size: 2rem;
	line-height: 1.1;
}

.subtitle,
.hint,
.message {
	margin: 0;
	color: var(--muted);
	line-height: 1.5;
}

.hint {
	margin-top: 18px;
	font-size: 0.95rem;
}

.field {
	margin-top: 18px;
}

label {
	display: block;
	margin-bottom: 8px;
	font-weight: 700;
	font-size: 0.95rem;
}

input {
	width: 100%;
	padding: 14px 16px;
	border: 1px solid var(--border);
	border-radius: 14px;
	font-size: 1rem;
	background: #fff;
	color: var(--text);
}

input:focus {
	outline: 2px solid rgba(180, 83, 9, 0.18);
	border-color: var(--accent);
}

.actions {
	display: flex;
	gap: 12px;
	margin-top: 22px;
	flex-wrap: wrap;
}

.button {
	appearance: none;
	border: 0;
	border-radius: 999px;
	padding: 13px 18px;
	text-decoration: none;
	font-weight: 700;
	cursor: pointer;
	transition: transform 0.15s ease, background 0.15s ease, color 0.15s ease;
}

.button:hover {
	transform: translateY(-1px);
}

.button.primary {
	background: var(--accent);
	color: #fff;
}

.button.primary:hover {
	background: var(--accent-dark);
}

.button.secondary {
	background: #f3f4f6;
	color: var(--text);
}

.message {
	margin-top: 18px;
	min-height: 24px;
	font-weight: 700;
}

.message.error {
	color: #b91c1c;
}

.message.success {
	color: #166534;
}

.token-box {
	margin-top: 18px;
	padding: 16px;
	border-radius: 14px;
	background: #f9fafb;
	border: 1px dashed var(--border);
	word-break: break-all;
	font-size: 0.92rem;
}
```
