# AGENTS.md

## Project overview

**DoveL'hoMesso** is a native Android app (Kotlin + Jetpack Compose) for household item/document organization. Single-module Gradle project (`app/`), fully offline with Room (SQLite) database. Italian-language UI.

## Cursor Cloud specific instructions

### Prerequisites

- **JDK 17** (`/usr/lib/jvm/java-17-openjdk-amd64`) — required by `compileOptions` in `app/build.gradle.kts`. Set `JAVA_HOME` accordingly.
- **Android SDK** at `/opt/android-sdk` with platform 34, build-tools 34.0.0, and platform-tools. `local.properties` must contain `sdk.dir=/opt/android-sdk`.
- The Gradle wrapper (`./gradlew`) handles Gradle 8.5 automatically.

### Common commands

| Task | Command |
|---|---|
| Build debug APK | `JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew assembleDebug` |
| Run lint | `JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew lint` |
| Run unit tests | `JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew testDebugUnitTest` |

### Emulator (no KVM)

The Cloud VM does not have KVM support. Running the Android emulator with `-no-accel` works but is extremely slow (~8 min boot, frequent ANR dialogs for System UI). Key flags for headless emulator:

```
emulator -avd test_device -no-window -no-audio -no-boot-anim -no-accel -gpu swiftshader_indirect -memory 2048 -partition-size 4096 -no-snapshot -wipe-data -no-metrics &
```

After boot (`adb shell getprop sys.boot_completed` returns `1`), keep screen awake with:

```
adb shell svc power stayon true
adb shell settings put system screen_off_timeout 2147483647
```

### Gotchas

- No unit or instrumentation tests exist in the project (`src/test/`, `src/androidTest/` are absent).
- `local.properties` is gitignored; the update script recreates it on each run.
- Configuration cache is enabled (`gradle.properties`). If you change build plugins or build logic, you may need `./gradlew --no-configuration-cache` for the first build after.
