$root = Split-Path -Parent $MyInvocation.MyCommand.Path

Write-Host "Informe a senha do MySQL para DB_PASSWORD" -ForegroundColor Yellow
$dbPassword = Read-Host "DB_PASSWORD"

Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$root\\user-service'; `$env:DB_PASSWORD='$dbPassword'; mvn spring-boot:run"
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$root\\email-service'; `$env:DB_PASSWORD='$dbPassword'; mvn spring-boot:run"
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$root\\frontend'; npm install; npm start"

Write-Host "Serviços iniciados em janelas separadas." -ForegroundColor Green
