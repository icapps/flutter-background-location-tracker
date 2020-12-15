import 'package:background_location_tracker/background_location_tracker.dart';
import 'package:background_location_tracker/src/util/logger.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

class BackgroundChannel {
  static const _BACKGROUND_CHANNEL_NAME = 'com.icapps.background_location_tracker/background_channel';

  static void handleBackgroundUpdated(LocationUpdateCallback callback, {bool enableLogging = false}) {
    WidgetsFlutterBinding.ensureInitialized();
    final _backgroundChannel = const MethodChannel(_BACKGROUND_CHANNEL_NAME)
      ..setMethodCallHandler((call) async {
        switch (call.method) {
          case 'onLocationUpdate':
            await handleLocationUpdate(call, callback, enableLogging: enableLogging);
            break;
          default:
            break;
        }
      });
    BackgroundLocationTrackerLogger.log('handleBackgroundUpdated initialized');
    _backgroundChannel.invokeListMethod<void>(
      'initialized',
      {
        'enableLogging': enableLogging,
      },
    );
  }

  static Future<void> handleLocationUpdate(MethodCall call, LocationUpdateCallback callback, {bool enableLogging = false}) async {
    BackgroundLocationTrackerLogger.log('locationUpdate: ${call.arguments}');
    final data = call.arguments as Map<dynamic, dynamic>; // ignore: avoid_as
    final lat = data['lat'] as double; // ignore: avoid_as
    final lon = data['lon'] as double; // ignore: avoid_as
    callback(BackgroundLocationUpdateData(lat: lat, lon: lon));
  }
}
