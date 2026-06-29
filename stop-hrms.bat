@echo off
setlocal

set PORT=8080

echo Stopping HR Management System on port %PORT%...

powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$port = %PORT%; " ^
  "$projectPath = (Resolve-Path '%~dp0').Path.TrimEnd('\'); " ^
  "$connections = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue; " ^
  "$ids = @(); " ^
  "if ($connections) { $ids += $connections | Select-Object -ExpandProperty OwningProcess -Unique }; " ^
  "$related = Get-CimInstance Win32_Process | Where-Object { $_.CommandLine -and $_.CommandLine -notlike '*stop-hrms.bat*' -and ($_.CommandLine -like '*start-hrms*' -or $_.CommandLine -like '*mvnw.cmd*' -or $_.CommandLine -like '*spring-boot:run*' -or $_.CommandLine -like '*hr-management-system*' -or $_.CommandLine -like ('*' + $projectPath + '*')) }; " ^
  "if ($related) { $ids += $related | Select-Object -ExpandProperty ProcessId -Unique }; " ^
  "$ids = $ids | Sort-Object -Unique; " ^
  "if (-not $ids) { Write-Host 'No running HRMS process found.'; exit 0 }; " ^
  "foreach ($processId in $ids) { " ^
  "  Write-Host 'Killing process tree PID' $processId; " ^
  "  cmd /c taskkill /PID $processId /T /F; " ^
  "}"

echo.
echo Checking port %PORT% again...
netstat -ano | findstr :%PORT% | findstr LISTENING
if errorlevel 1 (
    echo HRMS stopped successfully.
) else (
    echo Port %PORT% is still in use. Run this file as Administrator if needed.
)
