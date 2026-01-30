# TruVision
## Connectivity

- Primary base URL (USB tether): http://192.168.42.42:8000
- Pi backend binds to 0.0.0.0:8000 so the same endpoints work over all interfaces.
- The Android app performs a USB health check at:
  - http://192.168.42.42:8000/health

### Advanced (debug only) – Override base URL

During development you may temporarily point the Android app to a different base URL
(for example, a local test server). This override is for debugging only and must not
be used in production builds.
