## 1.4.2 - 11-09-2023
- Added fromJson/toJson/toString/equals/copyWith methods in BackgroundLocationUpdateData
  
## 1.4.1 - 17-06-2023
- Updates Play Services Location library
  
## 1.4.0 - 11-01-2023
- Added extra fields. (-1 if not supported or not available)
    - horizontalAccuracy
    - alt
    - verticalAccuracy (Android > O)
    - course
    - courseAccuracy (iOS > 13.4 & Android > O)
    - speed
    - speedAccuracy (Android > O)

## 1.3.1 - 14-12-2022
- Fix crash with starting service in background

## 1.3.0 - 12-09-2022
- Added support for android sdk 33
- Updated dependencies
- Updated kotlin
- Fixed the breaking changes on the native android side
- Updated the example project to save the background locations to shared prefs
- Updated the example project & readme to use the `@pragma('vm:entry-point')` annotation

## 1.2.0 - 15-11-2021
- Added iOS specific options to restart the tracking after killing the app
- Added option to Android specific options to only get updates every x meters

## 1.1.0 - 14-10-2021
#Added
- Added more options to android config to specify update interval
- Added iOS specific options to control activity type and/or distance filter

## 1.0.2 - 22-06-2021
#Fixed
- Formatting

## 1.0.1 - 22-06-2021
#Added
- Pub.dev badge

## 1.0.0 - 22-06-2021
Initial release
- Background location tracking
- Nullsafety
- Android v1 & v2 embedding support
