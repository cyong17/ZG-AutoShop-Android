# ZG AutoShop Android — Beat FI V2 + Click 125

Android USB-OTG diagnostic prototype for:
- Honda Beat FI V2 — KB1H-N52 IN 01
- Honda Click 125 — K-Line PGM-FI

## Hardware
Phone/tablet must support USB Host/OTG. Use a supported FTDI FT232 USB serial adapter plus a proper automotive K-Line transceiver. Do NOT connect FT232 TTL directly to motorcycle K-Line.

## Build
Open this folder in Android Studio and build the `app` module. The project uses `usb-serial-for-android` 3.11.0 for Android USB serial/FTDI support.

## Current scope
Read-only diagnostic prototype: USB device detection, 10400 8N1 serial transport, bike selection, Honda K-Line initialization placeholder, raw table request, raw response/logging.

DTC decoding, live-data decoding, ECU reset, clearing DTCs, and ECU flashing are intentionally not enabled until exact frames and data mappings for the physical ECUs are verified.
