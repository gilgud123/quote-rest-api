# PowerShell script to run all tests with database setup

param(
    [switch]$SkipFrontend,
    [switch]$BackendOnly,
    [switch]$FrontendOnly
)

$ErrorActionPreference = "Stop"

Write-Host ""
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  Quote REST API - Test Runner" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

# Get project root directory
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = Split-Path -Parent $scriptDir

# Function to check if Docker is running
function Test-DockerRunning {
    try {
        docker info 2>&1 | Out-Null
        return $LASTEXITCODE -eq 0
    } catch {
        return $false
    }
}

# Function to start test database
function Start-TestDatabase {
    Write-Host "Starting PostgreSQL test database..." -ForegroundColor Yellow
    
    # Check if container already exists
    $containerExists = docker ps -a --format "{{.Names}}" | Select-String -Pattern "^quote-test-db$" -Quiet
    
    if ($containerExists) {
        Write-Host "Container 'quote-test-db' already exists. Starting it..." -ForegroundColor Gray
        docker start quote-test-db | Out-Null
    } else {
        Write-Host "Creating new PostgreSQL test container..." -ForegroundColor Gray
        docker run -d --name quote-test-db `
          -e POSTGRES_DB=testdb `
          -e POSTGRES_USER=testuser `
          -e POSTGRES_PASSWORD=testpass `
          -p 5434:5432 `
          postgres:15-alpine | Out-Null
    }
    
    # Wait for PostgreSQL to be ready
    Write-Host "Waiting for PostgreSQL to be ready..." -ForegroundColor Gray
    Start-Sleep -Seconds 3
    
    $maxAttempts = 30
    $attempt = 0
    $ready = $false
    
    while (-not $ready -and $attempt -lt $maxAttempts) {
        $attempt++
        try {
            $result = docker exec quote-test-db pg_isready -U testuser -d testdb 2>&1
            if ($LASTEXITCODE -eq 0) {
                $ready = $true
            } else {
                Start-Sleep -Seconds 1
            }
        } catch {
            Start-Sleep -Seconds 1
        }
    }
    
    if ($ready) {
        Write-Host "✓ PostgreSQL test database is ready" -ForegroundColor Green
        return $true
    } else {
        Write-Host "✗ Failed to start PostgreSQL test database" -ForegroundColor Red
        return $false
    }
}

# Check Docker is running
if (-not (Test-DockerRunning)) {
    Write-Host "✗ Docker is not running. Please start Docker and try again." -ForegroundColor Red
    exit 1
}

# Start test database (unless frontend-only)
if (-not $FrontendOnly) {
    if (-not (Start-TestDatabase)) {
        exit 1
    }
    Write-Host ""
}

# Run backend tests
if (-not $FrontendOnly) {
    Write-Host "───────────────────────────────────────────────────────" -ForegroundColor Cyan
    Write-Host "  Running Backend Tests" -ForegroundColor Cyan
    Write-Host "───────────────────────────────────────────────────────" -ForegroundColor Cyan
    Write-Host ""
    
    Push-Location $projectRoot
    try {
        if ($BackendOnly) {
            mvn clean test -pl backend
        } else {
            mvn clean test
        }
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host ""
            Write-Host "✓ Backend tests completed successfully" -ForegroundColor Green
        } else {
            Write-Host ""
            Write-Host "✗ Backend tests failed" -ForegroundColor Red
            Pop-Location
            exit 1
        }
    } catch {
        Write-Host ""
        Write-Host "✗ Error running backend tests: $_" -ForegroundColor Red
        Pop-Location
        exit 1
    }
    Pop-Location
}

# Run frontend tests
if (-not $BackendOnly -and -not $SkipFrontend) {
    Write-Host ""
    Write-Host "───────────────────────────────────────────────────────" -ForegroundColor Cyan
    Write-Host "  Running Frontend Tests" -ForegroundColor Cyan
    Write-Host "───────────────────────────────────────────────────────" -ForegroundColor Cyan
    Write-Host ""
    
    Push-Location "$projectRoot\frontend"
    try {
        # Check if node_modules exists
        if (-not (Test-Path "node_modules")) {
            Write-Host "Installing npm dependencies..." -ForegroundColor Yellow
            npm install
            if ($LASTEXITCODE -ne 0) {
                Write-Host "✗ npm install failed" -ForegroundColor Red
                Pop-Location
                exit 1
            }
        }
        
        npm run test:ci
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host ""
            Write-Host "✓ Frontend tests completed successfully" -ForegroundColor Green
        } else {
            Write-Host ""
            Write-Host "✗ Frontend tests failed" -ForegroundColor Red
            Pop-Location
            exit 1
        }
    } catch {
        Write-Host ""
        Write-Host "✗ Error running frontend tests: $_" -ForegroundColor Red
        Pop-Location
        exit 1
    }
    Pop-Location
}

# Cleanup test database
if (-not $FrontendOnly) {
    Write-Host ""
    Write-Host "───────────────────────────────────────────────────────" -ForegroundColor Cyan
    Write-Host "  Cleaning Up Test Database" -ForegroundColor Cyan
    Write-Host "───────────────────────────────────────────────────────" -ForegroundColor Cyan
    Write-Host ""
    
    Write-Host "Stopping and removing test database container..." -ForegroundColor Yellow
    
    # Stop and remove container
    docker stop quote-test-db 2>&1 | Out-Null
    docker rm -v quote-test-db 2>&1 | Out-Null
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Test database cleaned up successfully" -ForegroundColor Green
    } else {
        Write-Host "⚠ Warning: Failed to cleanup test database (may not exist)" -ForegroundColor Yellow
    }
}

# Summary
Write-Host ""
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Green
Write-Host "  All Tests Completed Successfully! ✓" -ForegroundColor Green
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Green
Write-Host ""

if (-not $FrontendOnly) {
    Write-Host "Backend Coverage Report:" -ForegroundColor White
    Write-Host "  file:///$projectRoot/backend/target/site/jacoco/index.html" -ForegroundColor Cyan
}

if (-not $BackendOnly) {
    Write-Host "Frontend Coverage Report:" -ForegroundColor White
    Write-Host "  file:///$projectRoot/frontend/coverage/quote-frontend/index.html" -ForegroundColor Cyan
}

Write-Host ""
