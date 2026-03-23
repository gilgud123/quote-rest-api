#!/usr/bin/env pwsh
# Stop frontend development environment

Write-Host "🛑 Stopping Quote REST API - Frontend Development Environment" -ForegroundColor Yellow
Write-Host ""

# Stop Docker services
Write-Host "📦 Stopping all services..." -ForegroundColor White
docker-compose -f docker-compose-frontend.yml down

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✅ All services stopped!" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "⚠️  Some services may not have stopped correctly" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "💡 Useful commands:" -ForegroundColor Cyan
Write-Host "   Remove volumes (clean database): " -NoNewline -ForegroundColor White
Write-Host "docker-compose -f docker-compose-frontend.yml down -v" -ForegroundColor Gray
Write-Host "   View stopped containers:         " -NoNewline -ForegroundColor White
Write-Host "docker ps -a" -ForegroundColor Gray
Write-Host "   Remove all stopped containers:   " -NoNewline -ForegroundColor White
Write-Host "docker container prune" -ForegroundColor Gray
