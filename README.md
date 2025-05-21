# background_location_tracker

A new Flutter plugin that allows you to track the background location for Android & iOS

[![Build Status](https://travis-ci.com/icapps/flutter-background-location-tracker.svg?branch=master)](https://travis-ci.com/icapps/flutter-background-location-tracker)
[![Coverage Status](https://coveralls.io/repos/github/icapps/flutter-background-location-tracker/badge.svg?branch=master)](https://coveralls.io/github/icapps/flutter-background-location-tracker?branch=master)
[![pub package](https://img.shields.io/pub/v/background_location_tracker.svg)](https://pub.dartlang.org/packages/background_location_tracker)

## Android Config

### Update compile sdk

Compile sdk should be at 29 at least.
```
android {
  ...
  compileSdkVersion 29
  ...

  defaultConfig {
    ...
    targetSdkVersion 29
    ...
  }
  ...
}
```

## iOS Configuration

### Update Info.plist

Add the correct permission descriptions
```
	<key>NSLocationAlwaysAndWhenInUseUsageDescription</key>
	<string>Your description why you should use NSLocationAlwaysAndWhenInUseUsageDescription</string>
	<key>NSLocationAlwaysUsageDescription</key>
	<string>Your description why you should use NSLocationAlwaysAndWhenInUseUsageDescription</string>
	<key>NSLocationWhenInUseUsageDescription</key>
	<string>Your description why you should use NSLocationAlwaysAndWhenInUseUsageDescription</string>
```

Add the background location updates in xcode

Or add the info to the Info.plist

```
	<key>UIBackgroundModes</key>
	<array>
		<string>location</string>
	</array>
```

### Update the AppDelegate

```
import UIKit
import Flutter
import background_location_tracker

@UIApplicationMain
@objc class AppDelegate: FlutterAppDelegate {
    override func application(_ application: UIApplication,didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        GeneratedPluginRegistrant.register(with: self)

        // Register plugins for background execution - this method is deprecated 
        // but kept for backward compatibility
        BackgroundLocationTrackerPlugin.setPluginRegistrantCallback { registry in
            GeneratedPluginRegistrant.register(with: registry)
        }

        return super.application(application, didFinishLaunchingWithOptions: launchOptions)
    }
}
```

## Flutter implementation

Make sure you set the `@pragma('vm:entry-point')` to make sure you can find the callback in release.

```
@pragma('vm:entry-point')
void backgroundCallback() {
  BackgroundLocationTrackerManager.handleBackgroundUpdated(
    (data) async => Repo().update(data),
  );
}

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await BackgroundLocationTrackerManager.initialize(
    backgroundCallback,
    config: const BackgroundLocationTrackerConfig(
      loggingEnabled: true,
      androidConfig: AndroidConfig(
        notificationIcon: 'explore',
        trackingInterval: Duration(seconds: 4),
        distanceFilterMeters: null,
      ),
      iOSConfig: IOSConfig(
        activityType: ActivityType.FITNESS,
        distanceFilterMeters: null,
        restartAfterKill: true,
      ),
    ),
  );

  runApp(MyApp());
}

Future startLocationTracking() async {
  await BackgroundLocationTrackerManager.startTracking();
}

Future stopLocationTracking() async {
  await BackgroundLocationTrackerManager.stopTracking();
}

```

# FAQ:

#### I get a Unhandled Exception: MissingPluginException(No implementation found for method .... on channel ...)

```
This is mostly caused by a misconfiguration of the plugin:
- Android: This plugin now uses Flutter's Plugin V2 embedding pattern (FlutterPluginBinding) so you don't need to set a manual plugin registrant callback.
- iOS: For compatibility with older Flutter versions, the setPluginRegistrantCallback is still available but will be removed in a future version.

If you're using Flutter 1.12 or later, the plugin should work out of the box with the V2 embedding.
```

## Migration Guide

### Upgrading from older versions

This plugin has been updated to use the modern Flutter Plugin V2 embedding pattern with FlutterPluginBinding. If you're using Flutter 1.12 or later, you don't need to make any changes to your app's code.

For backward compatibility, the older methods are still available but marked as deprecated:

- `BackgroundLocationTrackerPlugin.setPluginRegistrantCallback` on Android
- `BackgroundLocationTrackerPlugin.setPluginRegistrantCallback` on iOS

These methods will be removed in a future version of the plugin.
