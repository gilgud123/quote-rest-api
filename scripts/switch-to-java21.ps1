# Switch to Java 21 - PowerShell version
# Run as Administrator: Right-click PowerShell -> "Run as Administrator"

Write-Host "========================================"
Write-Host "Switching System to Java 21" -ForegroundColor Green
Write-Host "========================================"
Write-Host ""

# Set Machine-level JAVA_HOME (requires admin)
try {
    [System.Environment]::SetEnvironmentVariable('JAVA_HOME', 'C:\Program Files\Java\jdk-21', 'Machine')
    Write-Host "✅ JAVA_HOME set to Java 21 (system-wide)" -ForegroundColor Green
} catch {
    Write-Host "❌ Failed to set Machine-level JAVA_HOME. Make sure you run as Administrator!" -ForegroundColor Red
    Write-Host "Error: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "⚠️  IMPORTANT: You MUST do ONE of the following:" -ForegroundColor Yellow
Write-Host "   1. Close ALL terminal windows and open a NEW terminal" -ForegroundColor Yellow
Write-Host "   2. OR restart your IDE (IntelliJ IDEA)" -ForegroundColor Yellow
Write-Host "   3. OR restart your computer" -ForegroundColor Yellow
Write-Host ""
Write-Host "After restarting, verify with:" -ForegroundColor Cyan
Write-Host "   java -version" -ForegroundColor Cyan
Write-Host "   mvn -version" -ForegroundColor Cyan
Write-Host ""

# Verify current setting
$javaHome = [System.Environment]::GetEnvironmentVariable('JAVA_HOME', 'Machine')
Write-Host "Current Machine JAVA_HOME: $javaHome" -ForegroundColor Green

Read-Host "Press Enter to exit"
