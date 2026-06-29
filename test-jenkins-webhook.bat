@echo off
setlocal

if "%~1"=="" (
    echo Usage:
    echo   test-jenkins-webhook.bat http://YOUR_JENKINS_HOST:8080
    exit /b 1
)

set JENKINS_URL=%~1
set WEBHOOK_URL=%JENKINS_URL%/github-webhook/

echo Testing Jenkins GitHub webhook endpoint:
echo %WEBHOOK_URL%
echo.

powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$url = '%WEBHOOK_URL%'; " ^
  "$headers = @{ 'X-GitHub-Event' = 'ping'; 'User-Agent' = 'HRMS-Jenkins-Webhook-Test' }; " ^
  "$body = '{\"zen\":\"HRMS webhook test\"}'; " ^
  "try { " ^
  "  $response = Invoke-WebRequest -UseBasicParsing -Uri $url -Method Post -ContentType 'application/json' -Headers $headers -Body $body; " ^
  "  Write-Host ('Status: ' + $response.StatusCode); " ^
  "  Write-Host 'Webhook endpoint responded.'; " ^
  "} catch { " ^
  "  Write-Host ('Webhook test failed: ' + $_.Exception.Message); " ^
  "  exit 1; " ^
  "}"
