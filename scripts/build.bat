@echo off
setlocal enabledelayedexpansion

echo ===================================================
echo   SpendWise - Compiling Core Java Project
echo ===================================================

cd /d "%~dp0\.."

if not exist "bin" mkdir bin

echo [1/3] Finding all Java source files...
powershell -NoProfile -Command "Get-ChildItem -Path src\main\java, src\test\java -Filter *.java -Recurse | ForEach-Object { '\"' + $_.FullName.Replace('\', '/') + '\"' } | Set-Content -Encoding ascii sources.txt"

echo [2/3] Compiling sources with javac (Java 17)...
javac -encoding UTF-8 -d bin -cp "lib/*" @sources.txt

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed!
    if exist sources.txt del sources.txt
    exit /b %ERRORLEVEL%
)

if exist sources.txt del sources.txt

echo [3/3] Copying application resources (SQL, Web assets, Properties)...
xcopy /E /I /Y src\main\resources\* bin\ > nul

echo ===================================================
echo   [SUCCESS] SpendWise built successfully into /bin
echo ===================================================

