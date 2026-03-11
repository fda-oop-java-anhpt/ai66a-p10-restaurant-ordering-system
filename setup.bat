@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion

echo ============================================================
echo   Restaurant POS - Setup Script
echo ============================================================
echo.

:: ──────────────────────────────────────────────
:: 1. Check Java
:: ──────────────────────────────────────────────
echo [1/5] Checking Java...
java -version >nul 2>&1
if !ERRORLEVEL! neq 0 (
    echo    ❌ Java not found. Please install JDK 17+.
    echo    Download: https://adoptium.net/
    pause
    exit /b 1
)
for /f "tokens=3" %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do set "JAVA_VER=%%v"
echo    ✅ Java found: !JAVA_VER!
echo.

:: ──────────────────────────────────────────────
:: 2. Check Maven
:: ──────────────────────────────────────────────
echo [2/5] Checking Maven...
set "MVN_CMD="

:: Try PATH first
where mvn >nul 2>&1
if !ERRORLEVEL! equ 0 (
    for /f "delims=" %%p in ('where mvn') do set "MVN_CMD=%%p"
    goto :mvn_found
)

echo    Maven not in PATH. Searching...

:: Check env vars
if defined MAVEN_HOME if exist "!MAVEN_HOME!\bin\mvn.cmd" (
    set "MVN_CMD=!MAVEN_HOME!\bin\mvn.cmd"
    goto :mvn_found
)
if defined Maven if exist "!Maven!\mvn.cmd" (
    set "MVN_CMD=!Maven!\mvn.cmd"
    goto :mvn_found
)
if defined M2_HOME if exist "!M2_HOME!\bin\mvn.cmd" (
    set "MVN_CMD=!M2_HOME!\bin\mvn.cmd"
    goto :mvn_found
)

:: Search Downloads folder
for /d %%a in ("%USERPROFILE%\Downloads\apache-maven-*") do (
    for /d %%b in ("%%~a\apache-maven-*") do (
        if exist "%%~b\bin\mvn.cmd" set "MVN_CMD=%%~b\bin\mvn.cmd"
    )
    if exist "%%~a\bin\mvn.cmd" set "MVN_CMD=%%~a\bin\mvn.cmd"
)
if defined MVN_CMD goto :mvn_found

:: Search Program Files
for /d %%a in ("C:\Program Files\apache-maven-*") do (
    if exist "%%~a\bin\mvn.cmd" set "MVN_CMD=%%~a\bin\mvn.cmd"
)

:mvn_found
if not defined MVN_CMD (
    echo    ❌ Maven not found. Please install Maven 3.8+.
    echo    Download: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)
echo    ✅ Maven found.
echo    Path: !MVN_CMD!
echo.

:: ──────────────────────────────────────────────
:: 3. Check PostgreSQL (psql)
:: ──────────────────────────────────────────────
echo [3/5] Checking PostgreSQL...
set "PSQL_CMD="

where psql >nul 2>&1
if !ERRORLEVEL! equ 0 (
    for /f "delims=" %%p in ('where psql') do set "PSQL_CMD=%%p"
    goto :psql_found
)

echo    psql not in PATH. Searching...
for /d %%d in ("C:\Program Files\PostgreSQL\*") do (
    if exist "%%~d\bin\psql.exe" set "PSQL_CMD=%%~d\bin\psql.exe"
)
if not defined PSQL_CMD (
    for /d %%d in ("C:\Program Files (x86)\PostgreSQL\*") do (
        if exist "%%~d\bin\psql.exe" set "PSQL_CMD=%%~d\bin\psql.exe"
    )
)

:psql_found
if not defined PSQL_CMD (
    echo    ❌ PostgreSQL not found. Please install PostgreSQL 14+.
    echo    Download: https://www.postgresql.org/download/windows/
    pause
    exit /b 1
)
echo    ✅ psql found.
echo    Path: !PSQL_CMD!
echo.

:: ──────────────────────────────────────────────
:: 4. Setup Database
:: ──────────────────────────────────────────────
echo [4/5] Setting up PostgreSQL database...
echo.

set "DB_HOST=localhost"
set "DB_PORT=5432"
set "DB_NAME=restaurant_pos"
set "DB_USER=postgres"

set /p "DB_HOST=   PostgreSQL Host [!DB_HOST!]: " || set "DB_HOST=localhost"
set /p "DB_PORT=   PostgreSQL Port [!DB_PORT!]: " || set "DB_PORT=5432"
set /p "DB_USER=   PostgreSQL User [!DB_USER!]: " || set "DB_USER=postgres"
set /p "DB_PASSWORD=   PostgreSQL Password: "
echo.

:: Set PGPASSWORD so psql never prompts
set "PGPASSWORD=!DB_PASSWORD!"

:: Check if database exists
echo    Checking if database '!DB_NAME!' exists...
"!PSQL_CMD!" -h !DB_HOST! -p !DB_PORT! -U !DB_USER! -lqt 2>nul | findstr /i "!DB_NAME!" >nul 2>&1
if !ERRORLEVEL! equ 0 (
    echo    ⚠️  Database '!DB_NAME!' already exists.
    set /p "RECREATE=   Recreate database? This will DROP and re-create with schema + seed data (y/N): "
    if /i "!RECREATE!"=="y" (
        echo.
        echo    Dropping existing database...
        "!PSQL_CMD!" -h !DB_HOST! -p !DB_PORT! -U !DB_USER! -c "DROP DATABASE !DB_NAME!;"
        if !ERRORLEVEL! neq 0 (
            echo    ❌ Failed to drop database. It may be in use. Close pgAdmin/other connections and retry.
            pause
            exit /b 1
        )
        echo    ✅ Database dropped.
    ) else (
        echo    Skipping database setup.
        goto :skip_db
    )
)

:: Create database
echo    Creating database '!DB_NAME!'...
"!PSQL_CMD!" -h !DB_HOST! -p !DB_PORT! -U !DB_USER! -c "CREATE DATABASE !DB_NAME!;"
if !ERRORLEVEL! neq 0 (
    echo    ❌ Failed to create database. Check your credentials.
    pause
    exit /b 1
)
echo    ✅ Database created.

:: Run schema
echo.
echo    Running schema.sql...
"!PSQL_CMD!" -h !DB_HOST! -p !DB_PORT! -U !DB_USER! -d !DB_NAME! -f "src\com\oop\project\db\schema.sql"
if !ERRORLEVEL! neq 0 (
    echo    ❌ Failed to run schema.sql
    pause
    exit /b 1
)
echo    ✅ Schema applied.

:: Run seed data
echo    Running seed_static.sql...
"!PSQL_CMD!" -h !DB_HOST! -p !DB_PORT! -U !DB_USER! -d !DB_NAME! -f "src\com\oop\project\db\seed_static.sql"
if !ERRORLEVEL! neq 0 (
    echo    ❌ Failed to run seed_static.sql
    pause
    exit /b 1
)
echo    ✅ Seed data loaded.
echo.

:skip_db

:: ──────────────────────────────────────────────
:: 5. Build with Maven
:: ──────────────────────────────────────────────
echo [5/5] Building project with Maven...
echo.
call "!MVN_CMD!" clean compile
if !ERRORLEVEL! neq 0 (
    echo.
    echo    ❌ Maven build failed. Check errors above.
    pause
    exit /b 1
)
echo.
echo    ✅ Build successful!
echo.

:: ──────────────────────────────────────────────
:: Save DB config for run.bat
:: ──────────────────────────────────────────────
> .env echo DB_URL=jdbc:postgresql://!DB_HOST!:!DB_PORT!/!DB_NAME!
>> .env echo DB_USER=!DB_USER!
>> .env echo DB_PASSWORD=!DB_PASSWORD!
echo    ✅ Database config saved to .env
echo.

echo ============================================================
echo   Setup complete! Run the app with: run.bat
echo ============================================================
echo.
pause
