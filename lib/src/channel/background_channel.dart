import 'package:background_location_tracker/background_location_tracker.dart';
import 'package:background_location_tracker/src/util/logger.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

class BackgroundChannel {
  static const _BACKGROUND_CHANNEL_NAME =
      'com.icapps.background_location_tracker/background_channel';
  static const _backgroundChannel = MethodChannel(_BACKGROUND_CHANNEL_NAME);

  static void handleBackgroundUpdated(LocationUpdateCallback callback,
      {bool enableLogging = false}) {
    WidgetsFlutterBinding.ensureInitialized();
    _backgroundChannel
      ..setMethodCallHandler((call) async {
        switch (call.method) {
          case 'onLocationUpdate':
            return handleLocationUpdate(call, callback,
                enableLogging: enableLogging);
          default:
            return false;
        }
      })
      ..invokeMethod<void>(
        'initialized',
      );
  }

  static Future<bool> handleLocationUpdate(
      MethodCall call, LocationUpdateCallback callback,
      {bool enableLogging = false}) async {
    final data = call.arguments as Map<dynamic, dynamic>; // ignore: avoid_as
    final isLoggingEnabled =
        data['logging_enabled'] as bool; // ignore: avoid_as
    BackgroundLocationTrackerLogger.enableLogging = isLoggingEnabled;
    BackgroundLocationTrackerLogger.log('locationUpdate: ${call.arguments}');
    final lat = data['lat'] as double; // ignore: avoid_as
    final lon = data['lon'] as double; // ignore: avoid_as
    final accuracy = data['accuracy'] as double;
    final date = data['date'] as String;
    await callback(BackgroundLocationUpdateData(lat: lat, lon: lon,accuracy: accuracy,date: date));
    return true;
  }
}
