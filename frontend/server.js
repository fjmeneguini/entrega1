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

app.get('/register', function (req, res) {
  res.sendFile(path.join(__dirname, 'public', 'register.html'));
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

app.post('/register', function (req, res) {
  var authHeader = req.headers.authorization;
  var name = (req.body.name || '').trim();
  var role = (req.body.role || '').trim();

  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ error: 'Token não informado.' });
  }
  if (!name || !role) {
    return res.status(400).json({ error: 'Nome e cargo são obrigatórios.' });
  }

  userServiceApi.post('/users/update-profile', { name: name, role: role }, {
    headers: { Authorization: authHeader }
  }).then(function (response) {
    res.json(response.data);
  }).catch(function (error) {
    var status = (error.response && error.response.status) || 500;
    var message = (error.response && error.response.data && error.response.data.error) || 'Falha ao atualizar perfil.';
    res.status(status).json({ error: message });
  });
});

app.get('/api/protected', function (req, res) {
  var authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ error: 'Token não informado.' });
  }

  userServiceApi.get('/users/test/customer', {
    headers: { Authorization: authHeader }
  }).then(function (response) {
    res.json(response.data);
  }).catch(function (error) {
    var status = (error.response && error.response.status) || 500;
    var message = (error.response && error.response.data && error.response.data.error) || 'Falha ao acessar endpoint protegido.';
    res.status(status).json({ error: message });
  });
});

app.get('/users/me', function (req, res) {
  var authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ error: 'Token não informado.' });
  }

  userServiceApi.get('/users/me', {
    headers: { Authorization: authHeader }
  }).then(function (response) {
    res.json(response.data);
  }).catch(function (error) {
    var status = (error.response && error.response.status) || 500;
    var message = (error.response && error.response.data && error.response.data.error) || 'Falha ao consultar perfil.';
    res.status(status).json({ error: message });
  });
});

app.get('/dashboard', function (req, res) {
  res.sendFile(path.join(__dirname, 'public', 'dashboard.html'));
});

app.listen(3000, function () {
  console.log('Frontend running on http://localhost:3000');
});
