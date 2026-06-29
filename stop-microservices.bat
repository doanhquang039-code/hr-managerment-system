@echo off
setlocal

cd /d "%~dp0"

echo Stopping HRMS microservices stack...
docker compose -f docker-compose.microservices.yml down
echo Done.
