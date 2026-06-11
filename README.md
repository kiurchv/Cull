# Cull

Photo culling app for Android (Pixel / Android 11+).

## Setup

1. Clone the repo
2. Open in Android Studio (Ladybug or newer)
3. Android Studio will prompt to generate the Gradle wrapper — accept
4. Or run: `gradle wrapper --gradle-version 8.9`
5. Build & run on device, or push to GitHub → Actions builds the APK automatically

## GitHub Actions

Push to `main` → APK appears in Actions → Artifacts → `cull-debug`

For signed release builds, add these secrets to your repo:
- `KEYSTORE_BASE64` — base64-encoded keystore file
- `KEY_ALIAS`
- `KEY_PASSWORD`  
- `STORE_PASSWORD`

## Architecture

- **Data**: MediaStore API, Room (pHash cache), HardLinkManager
- **Domain**: GroupingEngine (3-level: series→batch→duplicates), PHashEngine
- **UI**: Jetpack Compose, Hilt, Navigation
- **Background**: WorkManager for periodic photo indexing

## Album structure

Albums are stored as hardlinks in `DCIM/Albums/<name>/` on internal storage.
This means: zero extra disk space, and any app that reads MediaStore sees them.
