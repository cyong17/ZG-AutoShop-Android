# ZG AutoShop — APK-ready build

This project is prepared specifically for:

- Honda Beat FI V2 — ECU `KB1H-N52 IN 01`
- Honda Click 125 — K-Line PGM-FI

The Android application uses USB Host mode and the `usb-serial-for-android` library. It is configured for 10400 baud, 8N1 and includes raw-response capture.

## Build the APK

Open this folder in Android Studio and use:

**Build > Build APK(s)**

The resulting debug APK is:

`app/build/outputs/apk/debug/app-debug.apk`

Rename/copy it to:

`ZG_AutoShop.apk`

The included `INSTALL_ON_PHONE.md` contains the phone installation and hardware instructions.

## Safety / protocol status

This is an APK-ready diagnostic client, not a claim of universal Honda ECU support. The current app deliberately does not enable ECU writing/flashing or unverified DTC/clear/reset commands. Raw K-Line capture is available so exact ECU responses can be validated before adding those operations.
