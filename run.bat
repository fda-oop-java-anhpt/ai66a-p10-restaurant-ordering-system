@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion

echo ============================================================
echo   Restaurant POS - Starting Application
echo ============================================================
echo.

:: ──────────────────────────────────────────────
:: Find Maven
:: ──────────────────────────────────────────────
set "MVN_CMD="
where mvn >nul 2>&1
if !ERRORLEVEL! equ 0 (
    for /f "delims=" %%p in ('where mvn') do set "MVN_CMD=%%p"
    goto :run_mvn_found
)
if defined MAVEN_HOME if exist "!MAVEN_HOME!\bin\mvn.cmd" (
    set "MVN_CMD=!MAVEN_HOME!\bin\mvn.cmd"
    goto :run_mvn_found
)
if defined Maven if exist "!Maven!\mvn.cmd" (
    set "MVN_CMD=!Maven!\mvn.cmd"
    goto :run_mvn_found
)
if defined M2_HOME if exist "!M2_HOME!\bin\mvn.cmd" (
    set "MVN_CMD=!M2_HOME!\bin\mvn.cmd"
    goto :run_mvn_found
)
for /d %%a in ("%USERPROFILE%\Downloads\apache-maven-*") do (
    for /d %%b in ("%%~a\apache-maven-*") do (
        if exist "%%~b\bin\mvn.cmd" set "MVN_CMD=%%~b\bin\mvn.cmd"
    )
    if exist "%%~a\bin\mvn.cmd" set "MVN_CMD=%%~a\bin\mvn.cmd"
)

:run_mvn_found
if not defined MVN_CMD (
    echo ❌ Maven not found. Run setup.bat first or install Maven.
    pause
    exit /b 1
)

:: ──────────────────────────────────────────────
:: Load .env and set as environment variables
:: ──────────────────────────────────────────────
if exist .env (
    for /f "usebackq tokens=1,* delims==" %%a in (".env") do (
        set "%%a=%%b"
    )
    echo ✅ Loaded config from .env
) else (
    echo ⚠️  No .env file found. Using default DB config.
    echo    Run setup.bat first if you haven't set up the database.
)
echo.

:: ──────────────────────────────────────────────
:: Compile and run application
:: ──────────────────────────────────────────────
echo Starting Restaurant POS...
echo.
call "!MVN_CMD!" clean compile exec:java
echo.
echo Application closed.
pause
