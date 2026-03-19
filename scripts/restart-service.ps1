#!/usr/bin/env pwsh
# Restart specific service in frontend development environment

param(
    [Parameter(Mandatory=$false)]
    [string]$Service
)

if (-not $Service) {
    Write-Host "Usage: .\scripts\restart-service.ps1 -Service <service>" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Available services:" -ForegroundColor Cyan
    Write-Host "  postgres  " -NoNewline -ForegroundColor White
    Write-Host "- PostgreSQL database" -ForegroundColor Gray
    Write-Host "  keycloak  " -NoNewline -ForegroundColor White
    Write-Host "- Keycloak authentication server" -ForegroundColor Gray
    Write-Host "  backend   " -NoNewline -ForegroundColor White
    Write-Host "- Spring Boot backend API" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Example: " -NoNewline -ForegroundColor White
    Write-Host ".\scripts\restart-service.ps1 -Service backend" -ForegroundColor Gray
    exit 1
}

Write-Host "🔄 Restarting $Service..." -ForegroundColor Yellow
docker-compose -f docker-compose-frontend.yml restart $Service

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ $Service restarted successfully" -ForegroundColor Green
    Write-Host ""
    Write-Host "View logs with: " -NoNewline -ForegroundColor White
    Write-Host "docker-compose -f docker-compose-frontend.yml logs -f $Service" -ForegroundColor Gray
} else {
    Write-Host "❌ Failed to restart $Service" -ForegroundColor Red
    exit 1
}
