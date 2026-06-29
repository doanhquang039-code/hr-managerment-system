@echo off
setlocal

echo Stopping HRMS Docker test container...
docker rm -f hrms-jenkins-test
echo Done.
