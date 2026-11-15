@echo off
setlocal
cd /d "%~dp0"

echo ============================================================
echo   TeamUp - JavaFX 21 Complete UI and Database Verification
echo   SRM University-AP Amaravati
echo ============================================================
echo.

if not exist data mkdir data
if not exist target\javafx-cache mkdir target\javafx-cache

echo [1/10] Compiling JavaFX application with Maven...
call mvnw.cmd compile -q
if errorlevel 1 (
    echo.
    echo COMPILATION FAILED! Check compiler errors above.
    exit /b 1
)
echo       Maven compilation completed successfully.

echo.
echo [2/10] Running Database and Business Logic Verification...
java -cp "target\classes;lib\sqlite-jdbc-3.47.0.0.jar" teamup.db.Phase3Verify
if errorlevel 1 (
    echo.
    echo DATABASE VERIFICATION FAILED!
    exit /b 1
)

echo.
echo [3/10] Running Shell and Navigation Lifecycle Verification...
java "-Djavafx.cachedir=target/javafx-cache" ^
     --module-path "lib/javafx-base-21.0.2-win.jar;lib/javafx-controls-21.0.2-win.jar;lib/javafx-graphics-21.0.2-win.jar" ^
     --add-modules javafx.controls,javafx.graphics ^
     -cp "target/classes;lib/*" ^
     teamup.db.JavaFXVerify
if errorlevel 1 (
    echo.
    echo JAVAFX UI VERIFICATION FAILED!
    exit /b 1
)

echo.
echo [4/10] Running Redesigned Home Page (13 Sections) Verification...
java "-Djavafx.cachedir=target/javafx-cache" ^
     --module-path "lib/javafx-base-21.0.2-win.jar;lib/javafx-controls-21.0.2-win.jar;lib/javafx-graphics-21.0.2-win.jar" ^
     --add-modules javafx.controls,javafx.graphics ^
     -cp "target/classes;lib/*" ^
     teamup.db.HomePageVerify
if errorlevel 1 (
    echo.
    echo HOME PAGE VERIFICATION FAILED!
    exit /b 1
)

echo.
echo [5/10] Running Complete Application Navigation Flow Verification...
java "-Djavafx.cachedir=target/javafx-cache" ^
     --module-path "lib/javafx-base-21.0.2-win.jar;lib/javafx-controls-21.0.2-win.jar;lib/javafx-graphics-21.0.2-win.jar" ^
     --add-modules javafx.controls,javafx.graphics ^
     -cp "target/classes;lib/*" ^
     teamup.db.AllPagesNavigationTest
if errorlevel 1 (
    echo.
    echo NAVIGATION VERIFICATION FAILED!
    exit /b 1
)

echo.
echo [6/10] Running Join Activity and SQLite Transaction Verification...
java -cp "target\classes;lib\sqlite-jdbc-3.47.0.0.jar" teamup.db.JoinActivityVerify
if errorlevel 1 (
    echo.
    echo JOIN ACTIVITY VERIFICATION FAILED!
    exit /b 1
)

echo.
echo [7/10] Running Organizer Contact Information Verification...
java -cp "target\classes;lib\sqlite-jdbc-3.47.0.0.jar" teamup.db.ContactInfoVerify
if errorlevel 1 (
    echo.
    echo CONTACT INFO VERIFICATION FAILED!
    exit /b 1
)

echo.
echo [8/10] Running Student Welcome and Identification Screen Verification...
java "-Djavafx.cachedir=target/javafx-cache" ^
     --module-path "lib/javafx-base-21.0.2-win.jar;lib/javafx-controls-21.0.2-win.jar;lib/javafx-graphics-21.0.2-win.jar" ^
     --add-modules javafx.controls,javafx.graphics ^
     -cp "target/classes;lib/*" ^
     teamup.db.WelcomeScreenVerify
if errorlevel 1 (
    echo.
    echo WELCOME SCREEN VERIFICATION FAILED!
    exit /b 1
)

echo.
echo [9/10] Running Edit and Delete Activity Verification...
java "-Djavafx.cachedir=target/javafx-cache" ^
     --module-path "lib/javafx-base-21.0.2-win.jar;lib/javafx-controls-21.0.2-win.jar;lib/javafx-graphics-21.0.2-win.jar" ^
     --add-modules javafx.controls,javafx.graphics ^
     -cp "target/classes;lib/*" ^
     teamup.db.EditDeleteActivityVerify
if errorlevel 1 (
    echo.
    echo EDIT AND DELETE ACTIVITY VERIFICATION FAILED!
    exit /b 1
)

echo.
echo [10/10] Running Audit Bug Fixes Automated Verification...
java "-Djavafx.cachedir=target/javafx-cache" ^
     --module-path "lib/javafx-base-21.0.2-win.jar;lib/javafx-controls-21.0.2-win.jar;lib/javafx-graphics-21.0.2-win.jar" ^
     --add-modules javafx.controls,javafx.graphics ^
     -cp "target/classes;lib/*" ^
     teamup.db.AuditFixesVerify
if errorlevel 1 (
    echo.
    echo AUDIT BUG FIXES VERIFICATION FAILED!
    exit /b 1
)

echo.
echo ============================================================
echo   ALL VERIFICATION TESTS COMPLETED SUCCESSFULLY!
echo   TeamUp Entire Application, Edit/Delete, Join Workflow, Contact Info and Audit Fixes are 100%% verified.
echo ============================================================
endlocal
