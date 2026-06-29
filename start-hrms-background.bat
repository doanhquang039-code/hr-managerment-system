@echo off
setlocal

cd /d "%~dp0"

echo Starting HR Management System in background...
echo URL: http://localhost:8080

start "HRMS Server" /min cmd /c ""%~dp0mvnw.cmd" spring-boot:run"

timeout /t 3 >nul
start http://localhost:8080

echo HRMS is starting in a separate minimized window.
echo Use stop-hrms.bat to stop it.
