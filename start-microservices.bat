@echo off
setlocal

cd /d "%~dp0"

echo Starting HRMS microservices stack...
echo Gateway: http://localhost:8088
echo Notification service: http://localhost:8082
echo.

docker compose -f docker-compose.microservices.yml up -d --build

echo.
echo Done. Open http://localhost:8088/actuator/health
