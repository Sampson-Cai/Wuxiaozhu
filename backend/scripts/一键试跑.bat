@echo off
rem Run the API demo script in this folder
cd /d "%~dp0.."
for %%f in ("%~dp0*.ps1") do powershell -NoProfile -ExecutionPolicy Bypass -File "%%f"
echo.
pause
