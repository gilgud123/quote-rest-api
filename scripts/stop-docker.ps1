# Script to stop Docker Compose services (Windows)

Write-Host "`n? Stopping Quote REST API services...`n" -ForegroundColor Yellow
docker-compose down

Write-Host "`n? Services stopped!`n" -ForegroundColor Green

Write-Host "To remove all data volumes:" -ForegroundColor Cyan
Write-Host "  docker-compose down -v`n"
