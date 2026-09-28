# Install ZG AutoShop on Android

The final installable file is `ZG_AutoShop.apk`.

1. Build the project as a Debug APK using Android Studio: **Build > Build APK(s)**.
2. Copy `app/build/outputs/apk/debug/app-debug.apk` to the phone and rename it to `ZG_AutoShop.apk` if desired.
3. Open the APK on the phone and approve installation if Android asks for permission to install from that source.
4. Open **ZG AutoShop**.
5. Connect the Android phone to the USB serial/K-Line hardware through a USB-OTG adapter.

## Important hardware requirement

The FT232/USB-serial adapter is **not itself a vehicle K-Line electrical interface**. Use a proper K-Line transceiver/interface between the USB serial adapter and the motorcycle diagnostic K-Line.

Do not connect FT232 TTL TX/RX directly to the motorcycle K-Line.

## Current diagnostic scope

- Honda Beat FI V2 — KB1H-N52 IN 01
- Honda Click 125 — K-Line PGM-FI
- USB Host serial connection
- 10400 baud, 8N1
- raw K-Line response capture
- conservative diagnostic probing

ECU-specific DTC decoding, live-data decoding, reset/clear-DTC commands, and flashing remain disabled until exact frames are verified against the physical ECU.
