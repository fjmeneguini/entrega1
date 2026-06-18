$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = Join-Path $root "entrega1"

Write-Host "Informe a senha do MySQL para DB_PASSWORD" -ForegroundColor Yellow
$dbPassword = Read-Host "DB_PASSWORD"

Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$projectRoot\user-service'; `$env:DB_PASSWORD='$dbPassword'; mvn spring-boot:run"
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$projectRoot\email-service'; `$env:DB_PASSWORD='$dbPassword'; mvn spring-boot:run"
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$projectRoot\frontend'; npm install; npm start"

Write-Host "Servicos iniciados em janelas separadas." -ForegroundColor Green
