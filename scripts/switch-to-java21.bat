@echo off
REM Switch to Java 21 - Run this script as Administrator

echo ========================================
echo Switching System to Java 21
echo ========================================
echo.

REM Set Machine-level JAVA_HOME (requires admin)
setx JAVA_HOME "C:\Program Files\Java\jdk-21" /M

echo ✅ JAVA_HOME set to Java 21 (system-wide)
echo.
echo ⚠️  IMPORTANT: You MUST do ONE of the following:
echo    1. Close ALL terminal windows and open a NEW terminal
echo    2. OR restart your IDE (IntelliJ IDEA)
echo    3. OR restart your computer
echo.
echo After restarting, verify with: java -version
echo                                 mvn -version
echo.
pause
