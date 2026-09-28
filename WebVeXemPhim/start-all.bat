@echo off
cd /d "%~dp0"

if not exist "vexemphim-api\uploads" mkdir vexemphim-api\uploads

REM  start-all.bat       -> chay voi MySQL (phai bat service MySQL80 truoc)
REM  start-all.bat h2    -> chay voi H2 in-memory, khong can MySQL

set PROFILE_ARG=
if /i "%1"=="h2" set PROFILE_ARG=-Dspring-boot.run.profiles=h2

if defined PROFILE_ARG (
  echo [start-all] API dung H2 in-memory, du lieu reset moi lan chay.
) else (
  echo [start-all] API dung MySQL 3306, schema vexemphim. Neu MySQL chua bat: chay "start-all.bat h2".
)

start "vexemphim-api" cmd /k "cd vexemphim-api && mvn spring-boot:run %PROFILE_ARG%"
timeout /t 25
start "cineve-client" cmd /k "cd web-client && npm run dev"
timeout /t 8
start "" http://localhost:5173
