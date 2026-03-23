#!/usr/bin/env pwsh
# Quick start script for Angular frontend development
# Starts backend services and provides instructions for frontend

Write-Host "🚀 Starting Quote REST API - Frontend Development Environment" -ForegroundColor Cyan
Write-Host ""

# Check if Docker is running
Write-Host "📋 Checking Docker status..." -ForegroundColor Yellow
try {
    docker info > $null 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "Docker is not running"
    }
    Write-Host "✅ Docker is running" -ForegroundColor Green
} catch {
    Write-Host "❌ Error: Docker is not running. Please start Docker Desktop and try again." -ForegroundColor Red
    exit 1
}

# Start services
Write-Host ""
Write-Host "📦 Starting backend services (PostgreSQL, Keycloak, Backend API)..." -ForegroundColor Yellow
docker-compose -f docker-compose-frontend.yml up -d

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Failed to start services" -ForegroundColor Red
    exit 1
}

# Wait for services
Write-Host ""
Write-Host "⏳ Waiting for services to be healthy..." -ForegroundColor Yellow
Write-Host "   This may take 60-90 seconds for Keycloak initialization..." -ForegroundColor Gray

$maxWait = 180
$elapsed = 0
$healthy = $false

while ($elapsed -lt $maxWait -and -not $healthy) {
    Start-Sleep -Seconds 5
    $elapsed += 5
    
    $backendHealth = docker inspect --format='{{.State.Health.Status}}' quote-backend-frontend 2>$null
    $keycloakHealth = docker inspect --format='{{.State.Health.Status}}' quote-keycloak-frontend 2>$null
    $postgresHealth = docker inspect --format='{{.State.Health.Status}}' quote-postgres-frontend 2>$null
    
    if ($backendHealth -eq "healthy" -and $keycloakHealth -eq "healthy" -and $postgresHealth -eq "healthy") {
        $healthy = $true
    } else {
        Write-Host "   ⏳ Waiting... (${elapsed}s) - Backend: $backendHealth, Keycloak: $keycloakHealth, Postgres: $postgresHealth" -ForegroundColor Gray
    }
}

if (-not $healthy) {
    Write-Host ""
    Write-Host "⚠️  Services did not become healthy within ${maxWait}s" -ForegroundColor Yellow
    Write-Host "   You can check logs with: docker-compose -f docker-compose-frontend.yml logs" -ForegroundColor Gray
} else {
    Write-Host ""
    Write-Host "✅ All services are healthy!" -ForegroundColor Green
}

# Display access information
Write-Host ""
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "🌐 Service Access Points" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""
Write-Host "Backend API:      " -NoNewline -ForegroundColor White
Write-Host "http://localhost:8080/api/v1" -ForegroundColor Green
Write-Host "Swagger UI:       " -NoNewline -ForegroundColor White
Write-Host "http://localhost:8080/swagger-ui.html" -ForegroundColor Green
Write-Host "Keycloak Admin:   " -NoNewline -ForegroundColor White
Write-Host "http://localhost:9090" -ForegroundColor Green
Write-Host "  Credentials:    " -NoNewline -ForegroundColor White
Write-Host "admin / admin" -ForegroundColor Yellow
Write-Host ""
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "👤 Test Users (in Keycloak realm)" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""
Write-Host "Regular User:     " -NoNewline -ForegroundColor White
Write-Host "frontend-user / password" -ForegroundColor Green
Write-Host "Admin User:       " -NoNewline -ForegroundColor White
Write-Host "frontend-admin / password" -ForegroundColor Green
Write-Host ""
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "📝 Next Steps" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Open a new terminal" -ForegroundColor White
Write-Host "2. Navigate to frontend directory:" -ForegroundColor White
Write-Host "   cd frontend" -ForegroundColor Gray
Write-Host "3. Install dependencies (first time only):" -ForegroundColor White
Write-Host "   npm install" -ForegroundColor Gray
Write-Host "4. Start Angular development server:" -ForegroundColor White
Write-Host "   npm start" -ForegroundColor Gray
Write-Host "5. Open browser:" -ForegroundColor White
Write-Host "   http://localhost:4200" -ForegroundColor Green
Write-Host ""
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "🛠️  Useful Commands" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""
Write-Host "View logs:        " -NoNewline -ForegroundColor White
Write-Host "docker-compose -f docker-compose-frontend.yml logs -f" -ForegroundColor Gray
Write-Host "Stop services:    " -NoNewline -ForegroundColor White
Write-Host "docker-compose -f docker-compose-frontend.yml down" -ForegroundColor Gray
Write-Host "Restart service:  " -NoNewline -ForegroundColor White
Write-Host "docker-compose -f docker-compose-frontend.yml restart backend" -ForegroundColor Gray
Write-Host ""
Write-Host "Happy coding! 🎉" -ForegroundColor Cyan
