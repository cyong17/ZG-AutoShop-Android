@echo off
setlocal
cd /d "%~dp0"
if not exist "%ANDROID_HOME%" if not exist "%LOCALAPPDATA%\Android\Sdk" (
  echo Android SDK not found.
  echo Install Android Studio, then run this file again.
  pause
  exit /b 1
)
if exist "gradlew.bat" (
  call gradlew.bat assembleDebug
) else (
  echo Gradle wrapper is not included in this package.
  echo Open this folder in Android Studio and select Build ^> Build APK(s).
  pause
  exit /b 1
)
if exist "app\build\outputs\apk\debug\app-debug.apk" (
  copy /Y "app\build\outputs\apk\debug\app-debug.apk" "ZG_AutoShop.apk" >nul
  echo.
  echo READY: %CD%\ZG_AutoShop.apk
) else (
  echo APK build did not produce app-debug.apk.
)
pause
