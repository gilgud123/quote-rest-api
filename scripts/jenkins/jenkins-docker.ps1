# Jenkins Docker Management Script
# Provides easy commands to start, stop, and manage Jenkins container

param(
    [Parameter(Mandatory=$true)]
    [ValidateSet('start', 'stop', 'restart', 'logs', 'status', 'password')]
    [string]$Action
)

$ProjectRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$ComposeFile = Join-Path $ProjectRoot "docker-compose.yml"

function Start-Jenkins {
    Write-Host "Starting Jenkins..." -ForegroundColor Green
    docker compose -f $ComposeFile up -d jenkins
    
    Write-Host "`nJenkins is starting up..." -ForegroundColor Yellow
    Write-Host "This may take 1-2 minutes for initial setup." -ForegroundColor Yellow
    Write-Host "`nJenkins will be available at: http://localhost:8090" -ForegroundColor Cyan
    Write-Host "`nTo get the initial admin password, run:" -ForegroundColor Cyan
    Write-Host "  .\scripts\jenkins\jenkins-docker.ps1 password" -ForegroundColor White
}

function Stop-Jenkins {
    Write-Host "Stopping Jenkins..." -ForegroundColor Yellow
    docker compose -f $ComposeFile stop jenkins
    Write-Host "Jenkins stopped." -ForegroundColor Green
}

function Restart-Jenkins {
    Write-Host "Restarting Jenkins..." -ForegroundColor Yellow
    docker compose -f $ComposeFile restart jenkins
    Write-Host "Jenkins restarted." -ForegroundColor Green
}

function Show-Logs {
    Write-Host "Showing Jenkins logs (Ctrl+C to exit)..." -ForegroundColor Cyan
    docker compose -f $ComposeFile logs -f jenkins
}

function Show-Status {
    Write-Host "Jenkins Container Status:" -ForegroundColor Cyan
    docker ps --filter "name=quote-jenkins" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
    
    $container = docker ps --filter "name=quote-jenkins" --format "{{.Names}}"
    if ($container) {
        Write-Host "`nJenkins is RUNNING" -ForegroundColor Green
        Write-Host "Access at: http://localhost:8090" -ForegroundColor Cyan
    } else {
        Write-Host "`nJenkins is NOT running" -ForegroundColor Red
        Write-Host "Start it with: .\scripts\jenkins\jenkins-docker.ps1 start" -ForegroundColor Yellow
    }
}

function Get-AdminPassword {
    Write-Host "Retrieving Jenkins initial admin password..." -ForegroundColor Cyan
    
    $container = docker ps --filter "name=quote-jenkins" --format "{{.Names}}"
    if (-not $container) {
        Write-Host "ERROR: Jenkins container is not running!" -ForegroundColor Red
        Write-Host "Start it first with: .\scripts\jenkins\jenkins-docker.ps1 start" -ForegroundColor Yellow
        return
    }
    
    Write-Host "`nWaiting for Jenkins to initialize..." -ForegroundColor Yellow
    Start-Sleep -Seconds 5
    
    $password = docker exec quote-jenkins cat /var/jenkins_home/secrets/initialAdminPassword 2>$null
    
    if ($password) {
        Write-Host "`n============================================" -ForegroundColor Green
        Write-Host "Jenkins Initial Admin Password:" -ForegroundColor Green
        Write-Host $password -ForegroundColor White
        Write-Host "============================================`n" -ForegroundColor Green
        Write-Host "Use this password to unlock Jenkins at: http://localhost:8090" -ForegroundColor Cyan
    } else {
        Write-Host "`nPassword file not found yet. Jenkins may still be initializing." -ForegroundColor Yellow
        Write-Host "Wait 30-60 seconds and try again." -ForegroundColor Yellow
    }
}

# Execute requested action
switch ($Action) {
    'start'    { Start-Jenkins }
    'stop'     { Stop-Jenkins }
    'restart'  { Restart-Jenkins }
    'logs'     { Show-Logs }
    'status'   { Show-Status }
    'password' { Get-AdminPassword }
}
