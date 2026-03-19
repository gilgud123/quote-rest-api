# PowerShell script to start PostgreSQL test database for integration tests

Write-Host "Starting PostgreSQL test database..." -ForegroundColor Cyan

# Check if container already exists
$containerExists = docker ps -a --format "{{.Names}}" | Select-String -Pattern "^quote-test-db$" -Quiet

if ($containerExists) {
    Write-Host "Container 'quote-test-db' already exists. Starting it..." -ForegroundColor Yellow
    docker start quote-test-db | Out-Null
} else {
    Write-Host "Creating new PostgreSQL test container..." -ForegroundColor Yellow
    docker run -d --name quote-test-db `
      -e POSTGRES_DB=testdb `
      -e POSTGRES_USER=testuser `
      -e POSTGRES_PASSWORD=testpass `
      -p 5434:5432 `
      postgres:15-alpine | Out-Null
}

# Wait for PostgreSQL to be ready
Write-Host "Waiting for PostgreSQL to be ready..." -ForegroundColor Yellow
Start-Sleep -Seconds 3

# Check if PostgreSQL is accepting connections
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
            Write-Host "Waiting for PostgreSQL... (attempt $attempt/$maxAttempts)" -ForegroundColor Gray
            Start-Sleep -Seconds 1
        }
    } catch {
        Write-Host "Waiting for PostgreSQL... (attempt $attempt/$maxAttempts)" -ForegroundColor Gray
        Start-Sleep -Seconds 1
    }
}

if ($ready) {
    Write-Host ""
    Write-Host "✓ PostgreSQL test database is ready!" -ForegroundColor Green
    Write-Host "  Connection: jdbc:postgresql://localhost:5434/testdb" -ForegroundColor White
    Write-Host "  Username: testuser" -ForegroundColor White
    Write-Host "  Password: testpass" -ForegroundColor White
    Write-Host ""
    Write-Host "To stop the database, run: " -NoNewline -ForegroundColor White
    Write-Host "docker stop quote-test-db" -ForegroundColor Cyan
    Write-Host "To remove the database, run: " -NoNewline -ForegroundColor White
    Write-Host "docker rm -f quote-test-db" -ForegroundColor Cyan
} else {
    Write-Host ""
    Write-Host "✗ Failed to start PostgreSQL test database" -ForegroundColor Red
    exit 1
}
