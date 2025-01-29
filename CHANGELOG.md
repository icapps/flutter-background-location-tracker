## 1.5.0 - 2025-01-27
- Updated gradle dependencies
  
## 1.4.3 - 2024-03-28
- Updated targetSdkVersion to 34 and added FOREGROUND_SERVICE_LOCATION for support Android 14
- Removed unrecognized_error_code from analysis_options.yaml
  
## 1.4.2 - 2023-09-11
- Added fromJson/toJson/toString/equals/copyWith methods in BackgroundLocationUpdateData
  
## 1.4.1 - 2023-06-17
- Updates Play Services Location library
  
## 1.4.0 - 2023-01-11
- Added extra fields. (-1 if not supported or not available)
    - horizontalAccuracy
    - alt
    - verticalAccuracy (Android > O)
    - course
    - courseAccuracy (iOS > 13.4 & Android > O)
    - speed
    - speedAccuracy (Android > O)

## 1.3.1 - 2022-12-14
- Fix crash with starting service in background

## 1.3.0 - 2022-09-12
- Added support for android sdk 33
- Updated dependencies
- Updated kotlin
- Fixed the breaking changes on the native android side
- Updated the example project to save the background locations to shared prefs
- Updated the example project & readme to use the `@pragma('vm:entry-point')` annotation

## 1.2.0 - 2021-11-15
- Added iOS specific options to restart the tracking after killing the app
- Added option to Android specific options to only get updates every x meters

## 1.1.0 - 2021-10-14
#Added
- Added more options to android config to specify update interval
- Added iOS specific options to control activity type and/or distance filter

## 1.0.2 - 2021-06-22
#Fixed
- Formatting

## 1.0.1 - 2021-06-22
#Added
- Pub.dev badge

## 1.0.0 - 2021-06-22
Initial release
- Background location tracking
- Nullsafety
- Android v1 & v2 embedding support
