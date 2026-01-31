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

## Week 2 - USB Discovery UX + Advanced Override

### Features Added

#### 1. USB-Only Connection Discovery
- Removed Wi-Fi-first logic; USB is now the only normal path
- Auto-discovery probes candidate IPs in this order:
  1. Last known good IP (from previous successful connection)
  2. Common USB DHCP ranges: `192.168.42.x`, `192.168.231.x`
- First IP returning HTTP 2xx from `/health` is selected

#### 2. Advanced Override Base URL (Settings → Advanced)
- **Mode: Default** - Uses auto USB discovery
- **Mode: Override** - Manual base URL entry (e.g., `http://192.168.231.240:8000`)
- When Override is ON, discovery is skipped; all requests use the override URL
- Useful for testing with emulator or different Pi setups

#### 3. Diagnostics Panel (Debug Builds Only)
- Visible only when `BuildConfig.DEBUG` is true
- Shows on Connection screen:
  - Last health status (Connected / Not connected / Checking...)
  - Last HTTP code (or --)
  - Last base URL used
  - Last request latency in milliseconds

#### 4. Persistent Last Successful IP
- Stored in SharedPreferences after each successful health check
- Automatically tried first on next app launch
- Falls back to other candidates if last-known IP fails

### Testing Week 2

1. **Normal USB Discovery Flow**
   - Plug phone into Pi, enable USB tethering
   - Open app, tap "Connect" (Connection screen)
   - Resolver auto-discovers working IP and shows "Connected / 2xx / http://<ip>:8000"

2. **Developer Override Flow**
   - Go to Settings → Advanced
   - Toggle Mode to "Override"
   - Enter test URL (e.g., `http://192.168.231.240:8000`)
   - Return to Connection screen and tap "Run USB health check"
   - App uses override URL, skipping discovery

3. **Debug Diagnostics**
   - In debug builds, Connection screen shows Diagnostics card
   - Displays last HTTP code, latency, and base URL for each check
   - Use with wireless ADB + logcat for USB debugging

