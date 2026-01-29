# TruVision Microplastic Detector

## Connectivity

Two fixed connectivity modes are supported. These base URLs are constants and must not change.

### Base URLs (locked)
- Wi‑Fi hotspot (wlan0): http://192.168.4.1:8000
- USB tether (usb0): http://192.168.42.42:8000

### Resolver rule (Android app)
1. Try Wi‑Fi first: GET http://192.168.4.1:8000/health
2. If it fails (timeout / non‑2xx / no network), try USB: GET http://192.168.42.42:8000/health

### Pi-side requirement
Run the backend binding to all interfaces (host 0.0.0.0) so the same API works over both wlan0 and usb0.

### Advanced (debug only)
You may add an “Override base URL” field in the Android app Settings for debugging, but normal users should always use the resolver + constants above.
