# TruVision
## Connectivity

- Primary base URL (USB tether): http://192.168.231.240:8000
- Pi backend binds to 0.0.0.0:8000 so the same endpoints work over all interfaces.
- The Android app performs a USB health check at:
  - http://192.168.231.240:8000/health

### Advanced (debug only) – Override base URL

During development you may temporarily point the Android app to a different base URL
(for example, a local test server). This override is for debugging only and must not
be used in production builds.

## Week 1 – Run checklist (USB health)

1. Build Android debug APK  
   - In Android Studio: Build → Assemble Project, or  
   - From terminal:  
     ./gradlew app:assembleDebug  (run in android/Truvision)

2. Install APK on phone (USB debugging ON)  
   "$HOME/Library/Android/sdk/platform-tools/adb" install -r -t \
     "$HOME/Documents/Final_Year_Project/TruVision/android/Truvision/app/build/intermediates/apk/debug/app-debug.apk"

3. Start Pi backend  
   - Power on Raspberry Pi.  
   - In Pi terminal, run:  
     uvicorn main:app --host 0.0.0.0 --port 8000

4. Verify Pi /health from Pi  
   curl -v http://192.168.231.240:8000/health
   - Expect a 2xx HTTP status code.

5. Enable USB tethering (phone → Pi)  
   - Connect phone to Pi via USB.  
   - On phone: enable USB tethering.  
   - On Pi, confirm interface usb0 has IP 192.168.231.240/24.

6. Test USB health from phone app  
   - Open TruVision app.  
   - On Connection screen, tap "Run USB health check" once.  
   - When backend is UP and tether ON:  
     Status: Connected, Last HTTP code: 2xx, Base URL: http://192.168.231.240:8000  
   - When backend is DOWN or tether OFF:  
     Status: Not connected, Last HTTP code: non-2xx or --, Base URL: --
