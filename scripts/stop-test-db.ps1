# PowerShell script to stop PostgreSQL test database

Write-Host "Stopping PostgreSQL test database..." -ForegroundColor Cyan
docker stop quote-test-db | Out-Null

Write-Host ""
Write-Host "✓ Test database stopped." -ForegroundColor Green
Write-Host "To start it again, run: " -NoNewline -ForegroundColor White
Write-Host ".\scripts\start-test-db.ps1" -ForegroundColor Cyan
Write-Host "To remove it completely, run: " -NoNewline -ForegroundColor White
Write-Host "docker rm quote-test-db" -ForegroundColor Cyan
