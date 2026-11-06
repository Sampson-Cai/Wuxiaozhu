@echo off
cd /d "%~dp0.."
echo ============================================
echo   cangxiaozhu local server
echo   http://127.0.0.1:18080  (captcha disabled)
echo   keep this window open, Ctrl+C to stop
echo ============================================
echo.
java -jar "ruoyi-admin/target/ruoyi-admin.jar" --server.port=18080 --shiro.user.captchaEnabled=false
echo.
echo server stopped
pause
