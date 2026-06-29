@echo off
setlocal

cd /d "%~dp0"

echo Starting HR Management System...
echo URL: http://localhost:8080
echo.

call "%~dp0mvnw.cmd" spring-boot:run

if errorlevel 1 (
    echo.
    echo Application failed to start. Please check the error above.
    pause
)
