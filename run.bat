@echo off
echo Building and running Algorithm Visualizer...
echo.

echo Compiling the project...
call mvn clean compile
if %errorlevel% neq 0 (
    echo Build failed!
    pause
    exit /b 1
)

echo.
echo Running the application...
call mvn javafx:run

pause
