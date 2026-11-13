@echo off
setlocal

echo ===================================================
echo   SpendWise - Starting Application Server
echo ===================================================

cd /d "%~dp0\.."

if not exist "bin\com\spendwise\Main.class" (
    echo [INFO] Bin directory not detected. Building project first...
    call scripts\build.bat
)

echo [INFO] Launching SpendWise Main...
java -cp "bin;lib/*" com.spendwise.Main

pause
