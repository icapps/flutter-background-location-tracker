import 'dart:async';

import 'package:background_location_tracker/src/channel/background_channel.dart';
import 'package:background_location_tracker/src/channel/foreground_channel.dart';
import 'package:background_location_tracker/src/model/background_location_update_data.dart';
import 'package:background_location_tracker/src/util/logger.dart';

typedef LocationUpdateCallback = void Function(BackgroundLocationUpdateData data);

class BackgroundLocationTrackerManager {
  static bool _enabledLogging = false;

  static Future<void> initialize(Function callback) => ForegroundChannel.initialize(callback, enableLogging: _enabledLogging);

  static Future<bool> isTracking() async => ForegroundChannel.isTracking();

  static Future<void> startTracking() async => ForegroundChannel.startTracking(enableLogging: _enabledLogging);

  static Future<void> stopTracking() async => ForegroundChannel.stopTracking(enableLogging: _enabledLogging);

  static void handleBackgroundUpdated(LocationUpdateCallback callback) => BackgroundChannel.handleBackgroundUpdated(callback, enableLogging: _enabledLogging);

  // ignore: avoid_positional_boolean_parameters
  static void setLogging(bool enableLogging) {
    _enabledLogging = enableLogging;
    BackgroundLocationTrackerLogger.enableLogging = _enabledLogging;
  }
}
