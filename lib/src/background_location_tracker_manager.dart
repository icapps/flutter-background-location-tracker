import 'dart:async';

import 'package:background_location_tracker/background_location_tracker.dart';
import 'package:background_location_tracker/src/channel/background_channel.dart';
import 'package:background_location_tracker/src/channel/foreground_channel.dart';
import 'package:background_location_tracker/src/util/logger.dart';

typedef LocationUpdateCallback = Future<void> Function(
    BackgroundLocationUpdateData data);

class BackgroundLocationTrackerManager {
  static Future<void> initialize(Function callback,
      {BackgroundLocationTrackerConfig? config}) {
    final pluginConfig = config ??= const BackgroundLocationTrackerConfig();
    BackgroundLocationTrackerLogger.enableLogging = pluginConfig.loggingEnabled;
    return ForegroundChannel.initialize(callback, config: pluginConfig);
  }

  static Future<bool> isTracking() async => ForegroundChannel.isTracking();

  /// Starts tracking with the interval and distance filter of the config that
  /// was passed to [initialize], or the interval that was last set with
  /// [updateTrackingInterval].
  ///
  /// [config] only overrides the notification options. The other values,
  /// including [AndroidConfig.trackingInterval] and
  /// [AndroidConfig.distanceFilterMeters], are ignored.
  static Future<void> startTracking({AndroidConfig? config}) async =>
      ForegroundChannel.startTracking(config: config);

  static Future<void> stopTracking() async => ForegroundChannel.stopTracking();

  /// Changes the interval in which location updates are requested, without
  /// restarting tracking. Can be called from the UI and from the background
  /// callback.
  ///
  /// When not tracking, the interval will be used the next time tracking starts.
  /// [initialize] resets it to [AndroidConfig.trackingInterval].
  ///
  /// The returned future completes with an [ArgumentError] if [interval] is
  /// shorter than 1 millisecond, and with a `PlatformException` if the interval
  /// could not be changed, e.g. because the location permission was lost.
  ///
  /// Only has an effect on Android, iOS has no location interval.
  static Future<void> updateTrackingInterval(Duration interval) async =>
      ForegroundChannel.updateTrackingInterval(interval);

  static void handleBackgroundUpdated(LocationUpdateCallback callback) =>
      BackgroundChannel.handleBackgroundUpdated(callback);
}
