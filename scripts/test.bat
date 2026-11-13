@echo off
setlocal

echo ===================================================
echo   SpendWise - Running JUnit 5 Test Suite
echo ===================================================

cd /d "%~dp0\.."

if not exist "bin\com\spendwise\UserEncapsulationTest.class" (
    echo [INFO] Bin directory not detected. Building project first...
    call scripts\build.bat
)

echo [INFO] Executing JUnit Platform Console Standalone Runner...
java -jar lib\junit-platform-console-standalone-1.10.2.jar execute --class-path "bin;lib/mysql-connector-j-8.3.0.jar" --scan-class-path

echo ===================================================
if "%1"=="--no-pause" goto :eof
if "%INTERACTIVE%"=="" pause

