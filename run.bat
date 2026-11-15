@echo off
setlocal
cd /d "%~dp0"

echo ============================================================
echo   TeamUp - Campus Activity and Teammate Finder
echo   SRM University-AP Amaravati (JavaFX 21 Desktop App)
echo ============================================================
echo.

if not exist data mkdir data
if not exist target\javafx-cache mkdir target\javafx-cache

REM Compile application with Maven to ensure latest changes are included
echo Compiling application with Maven...
call mvnw.cmd compile -q
if errorlevel 1 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo Starting TeamUp Desktop Application...
echo.

java "-Djavafx.cachedir=target/javafx-cache" ^
     --module-path "lib/javafx-base-21.0.2-win.jar;lib/javafx-controls-21.0.2-win.jar;lib/javafx-graphics-21.0.2-win.jar" ^
     --add-modules javafx.controls,javafx.graphics ^
     -cp "target/classes;lib/*" ^
     teamup.Main

if errorlevel 1 (
    echo.
    echo Application exited with code %errorlevel%.
    pause
)

endlocal
