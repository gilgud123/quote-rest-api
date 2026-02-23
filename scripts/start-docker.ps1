# Script to build and run the application with Docker Compose (Windows)

Write-Host "`n? Starting Quote REST API with Docker Compose...`n" -ForegroundColor Cyan

# Build and start services
docker-compose up --build -d

Write-Host "`n? Services starting...`n" -ForegroundColor Green
Write-Host "Waiting for services to be healthy..." -ForegroundColor Yellow

# Wait for PostgreSQL
Write-Host -NoNewline "Waiting for PostgreSQL"
$maxAttempts = 30
$attempt = 0
while ($attempt -lt $maxAttempts) {
    $result = docker-compose exec -T postgres pg_isready -U quoteuser -d quotedb 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host " ? Ready!" -ForegroundColor Green
        break
    }
    Write-Host -NoNewline "."
    Start-Sleep -Seconds 2
    $attempt++
}

if ($attempt -eq $maxAttempts) {
    Write-Host " ? Timeout!" -ForegroundColor Red
    exit 1
}

# Wait for application
Write-Host -NoNewline "Waiting for Application"
$attempt = 0
while ($attempt -lt $maxAttempts) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -TimeoutSec 2 -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            Write-Host " ? Ready!" -ForegroundColor Green
            break
        }
    } catch {
        # Ignore errors
    }
    Write-Host -NoNewline "."
    Start-Sleep -Seconds 3
    $attempt++
}

if ($attempt -eq $maxAttempts) {
    Write-Host " ??  Taking longer than expected (check logs)" -ForegroundColor Yellow
}

Write-Host "`n? Quote REST API is running!`n" -ForegroundColor Green

Write-Host "? Endpoints:" -ForegroundColor Cyan
Write-Host "  • API: http://localhost:8080/api/v1"
Write-Host "  • Swagger UI: http://localhost:8080/swagger-ui.html"
Write-Host "  • API Docs: http://localhost:8080/api-docs"
Write-Host "  • Health: http://localhost:8080/actuator/health"

Write-Host "`n? Database:" -ForegroundColor Cyan
Write-Host "  • Host: localhost"
Write-Host "  • Port: 5432"
Write-Host "  • Database: quotedb"
Write-Host "  • Username: quoteuser"
Write-Host "  • Password: quotepass"

Write-Host "`n? Useful Commands:" -ForegroundColor Cyan
Write-Host "  • View logs: docker-compose logs -f"
Write-Host "  • View app logs: docker-compose logs -f app"
Write-Host "  • View DB logs: docker-compose logs -f postgres"
Write-Host "  • Stop: docker-compose down"
Write-Host "  • Stop and remove data: docker-compose down -v"
Write-Host ""
