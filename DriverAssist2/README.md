# 🚗 Driver Assist – Android App

Auto-reject calls and send SMS replies while driving. Simple, focused, and effective.

---

## Changelog

### v1.1 (Bug Fixes)
- 🐛 **Fixed**: App was showing onboarding screen every time it was opened — now correctly skips to main screen after first setup
- 🐛 **Fixed**: Missing `androidx.cardview:cardview:1.0.0` dependency causing build failure
- 🧹 **Cleanup**: Removed unused `RECEIVE_BOOT_COMPLETED` permission from manifest

---

## Features

| Feature | Details |
|---|---|
| **One-tap toggle** | Single switch to enable/disable driving mode |
| **Auto-reject calls** | Incoming calls are silently disconnected |
| **Auto SMS** | Caller receives: *"Hi! I am driving. I will call you back shortly."* |
| **Call log** | Blocked calls list with timestamp (Call Log tab) |
| **Clear log** | One-tap to clear all blocked call history |

---

## Project Structure

```
DriverAssist/
├── app/src/main/
│   ├── AndroidManifest.xml              ← Permissions + receiver registration
│   ├── java/com/driverassist/
│   │   ├── MainActivity.kt              ← ViewPager2 + tab setup + permissions
│   │   ├── HomeFragment.kt              ← Toggle UI + status card
│   │   ├── CallLogFragment.kt           ← Blocked calls list
│   │   ├── CallReceiver.kt              ← Core logic: reject call + send SMS
│   │   ├── CallLogAdapter.kt            ← RecyclerView adapter
│   │   └── AppPrefs.kt                  ← SharedPreferences + call log storage
│   └── res/
│       ├── layout/
│       │   ├── activity_main.xml
│       │   ├── fragment_home.xml
│       │   ├── fragment_call_log.xml
│       │   └── item_call_log.xml
│       ├── drawable/                    ← Vector icons + selector drawables
│       └── values/
│           ├── colors.xml
│           ├── strings.xml
│           └── themes.xml
├── build.gradle
└── settings.gradle
```

---

## How It Works

```
Incoming Call
      │
      ▼
CallReceiver (BroadcastReceiver)
      │
      ├── Is Driving Mode ON? ──No──▶ Do nothing
      │
      └── Yes
            │
            ├─▶ TelecomManager.endCall()     ← Reject the call
            ├─▶ SmsManager.sendTextMessage() ← Send SMS to caller
            └─▶ AppPrefs.addCallLogEntry()   ← Save to log
```

---

## Setup Instructions

### 1. Open in Android Studio
- Open Android Studio → **File → Open** → select the `DriverAssist/` folder
- Wait for Gradle sync to complete

### 2. Build & Run
- Connect an Android device (API 26+, Android 8.0 Oreo or higher)
- Click **Run ▶** or press `Shift+F10`

### 3. Grant Permissions (on first launch)
The app will ask for:

| Permission | Why needed |
|---|---|
| `READ_PHONE_STATE` | Detect incoming calls |
| `ANSWER_PHONE_CALLS` | Reject/end incoming calls |
| `SEND_SMS` | Send auto-reply message to caller |

> ⚠️ **All three must be granted** for the app to work correctly.

---

## Permissions – Android 13+ Note

On Android 13 (API 33) and above, SMS permissions may require additional steps.
If SMS isn't sending, go to:
**Settings → Apps → Driver Assist → Permissions** and manually enable **SMS**.

---

## Call Rejection – Technical Note

The app uses `TelecomManager.endCall()` which requires the `ANSWER_PHONE_CALLS` permission (runtime permission on Android 8+). This is the modern, non-reflective approach recommended by Google.

On some heavily customized ROMs (certain Xiaomi/MIUI, OPPO/ColorOS), the broadcast for `PHONE_STATE` may be delayed. If calls aren't being rejected in time, the app may show a "missed call" before the disconnect — this is a platform limitation.

---

## SMS Message

The auto-sent SMS reads:
> *"Hi! I am currently driving. I will call you back shortly. – Driver Assist"*

To customize this, edit `SMS_MESSAGE` in `CallReceiver.kt`:
```kotlin
private const val SMS_MESSAGE = "Hi! I am currently driving. I will call you back shortly. – Driver Assist"
```

---

## Screens

```
┌─────────────────────────┐    ┌─────────────────────────┐
│  🚗  Driver Assist      │    │  🚗  Driver Assist      │
│ [Drive]    [Call Log]   │    │ [Drive]    [Call Log]   │
├─────────────────────────┤    ├─────────────────────────┤
│                         │    │  Blocked Calls          │
│  ┌──────────────────┐   │    │                         │
│  │ Driving Mode  ●  │   │    │  ┌──────────────────┐   │
│  │ Auto-reject calls│   │    │  │ 📵 +91 98765...  │   │
│  └──────────────────┘   │    │  │ 28 Feb, 10:30 AM │   │
│                         │    │  │           ✉ SMS  │   │
│  ┌──────────────────┐   │    │  └──────────────────┘   │
│  │ 🚗 [car icon]    │   │    │                         │
│  │ Driving Mode ON  │   │    │  ┌──────────────────┐   │
│  │ Calls rejected   │   │    │  │ 📵 +91 87654...  │   │
│  └──────────────────┘   │    │  │ 28 Feb, 09:15 AM │   │
│                         │    │  └──────────────────┘   │
│  ┌──────────────────┐   │    │                         │
│  │ What happens?    │   │    │  [Clear All]            │
│  │ 📵 Calls rejected│   │    │                         │
│  │ ✉ SMS sent       │   │    │                         │
│  │ 📋 Calls logged  │   │    │                         │
│  └──────────────────┘   │    │                         │
└─────────────────────────┘    └─────────────────────────┘
       Home / Drive tab               Call Log tab
```

---

## Requirements

- **Android Studio** Hedgehog (2023.1.1) or newer
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)
- **Language**: Kotlin
- **Physical device recommended** — emulators don't support real phone calls
