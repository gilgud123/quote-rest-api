#!/usr/bin/env pwsh
# View logs for all frontend development services

Write-Host "📋 Frontend Development Services Logs" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""
Write-Host "Press Ctrl+C to exit" -ForegroundColor Yellow
Write-Host ""

# Follow logs from all services
docker-compose -f docker-compose-frontend.yml logs -f --tail=100
