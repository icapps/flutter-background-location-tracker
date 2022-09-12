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

Make sure you call the `setPluginRegistrantCallback` so other plugins can be accessed in the background.

```
import UIKit
import Flutter
import background_location_tracker

@UIApplicationMain
@objc class AppDelegate: FlutterAppDelegate {
    override func application(_ application: UIApplication,didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        GeneratedPluginRegistrant.register(with: self)

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
```

# FAQ:

#### I get a Unhandled Exception: MissingPluginException(No implementation found for method .... on channel ...)

```
This is mostly caused by a misconfiguration of the plugin:
Android Pre v2 embedding: make sure the plugin registrant callback is set
Android v2 embedding: Log a new github issues. This
iOS: make sure the plugin registrant callback is set
```
