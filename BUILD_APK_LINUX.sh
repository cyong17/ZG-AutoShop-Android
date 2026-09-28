#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
if [ -x ./gradlew ]; then
  ./gradlew assembleDebug
else
  echo "Open this project in Android Studio and use Build > Build APK(s)."
  exit 1
fi
cp -f app/build/outputs/apk/debug/app-debug.apk ./ZG_AutoShop.apk
echo "READY: $(pwd)/ZG_AutoShop.apk"
