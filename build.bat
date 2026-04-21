@echo off
REM Waste Management Attendance System - Build and Deploy Script (Windows)
REM This script builds and deploys the Java application on Windows

setlocal enabledelayedexpansion

set PROJECT_NAME=Attendance System - Face Recognition
set PROJECT_DIR=%~dp0
set BUILD_DIR=%PROJECT_DIR%target
set LOG_DIR=%PROJECT_DIR%logs
set DATA_DIR=%PROJECT_DIR%data

cls
echo ========================================
echo %PROJECT_NAME%
echo Build and Deployment Script
echo ========================================
echo.

REM Function to print messages
setlocal

REM Check prerequisites
:check_prerequisites
echo [INFO] Checking prerequisites...

where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java is not installed or not in PATH
    exit /b 1
)

for /f "tokens=3" %%i in ('java -version 2^>^&1 ^| findstr /R "version"') do set JAVA_VERSION=%%i
echo [SUCCESS] Found Java version: %JAVA_VERSION%

where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Maven is not installed or not in PATH
    exit /b 1
)

for /f "tokens=3" %%i in ('mvn -version 2^>^&1 ^| findstr /R "Apache"') do set MVN_VERSION=%%i
echo [SUCCESS] Found Maven

REM Create directories
:create_directories
echo [INFO] Creating necessary directories...

if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"
if not exist "%DATA_DIR%\faces" mkdir "%DATA_DIR%\faces"
if not exist "%BUILD_DIR%" mkdir "%BUILD_DIR%"

echo [SUCCESS] Directories created
echo.

REM Parse command line arguments
if "%1"=="" goto build_all
if /i "%1"=="build" goto build_project
if /i "%1"=="setup" goto setup_database
if /i "%1"=="run" goto run_application
if /i "%1"=="clean" goto clean_build
if /i "%1"=="all" goto build_all
if /i "%1"=="help" goto show_help
goto unknown_option

:build_project
echo [INFO] Building project...
cd %PROJECT_DIR%
call mvn clean package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Build failed
    exit /b 1
)
echo [SUCCESS] Project built successfully
goto end

:setup_database
echo [INFO] Setting up database...
where mysql >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [WARNING] MySQL not found. Please run SQL setup manually:
    echo mysql -u root -p ^< %PROJECT_DIR%sql\database_setup.sql
    goto end
)

echo Please enter MySQL root password when prompted:
mysql -u root -p < "%PROJECT_DIR%sql\database_setup.sql"
echo [SUCCESS] Database setup completed
goto end

:run_application
echo [INFO] Starting application...

for /f "delims=" %%i in ('dir /b /s "%BUILD_DIR%\*.jar" 2^>nul') do set JAR_FILE=%%i

if not defined JAR_FILE (
    echo [ERROR] JAR file not found
    exit /b 1
)

echo [SUCCESS] Found JAR: %JAR_FILE%
java -Xmx512m -Xms256m -jar "%JAR_FILE%"
goto end

:clean_build
echo [INFO] Cleaning build artifacts...
cd %PROJECT_DIR%
call mvn clean
if exist "%BUILD_DIR%" rmdir /s /q "%BUILD_DIR%"
echo [SUCCESS] Cleaned
goto end

:build_all
call :create_directories
call :build_project
call :setup_database
call :run_application
goto end

:show_help
echo Usage: %0 [option]
echo.
echo Options:
echo   build     - Build the project
echo   setup     - Setup database
echo   run       - Run the application
echo   clean     - Clean build artifacts
echo   all       - Check prerequisites, build, setup DB, and run
echo   help      - Show this help message
echo.
goto end

:unknown_option
echo [ERROR] Unknown option: %1
echo Run "%0 help" for usage information
exit /b 1

:end
echo [SUCCESS] Operation completed successfully!
endlocal
