import 'dart:ui';
import 'package:background_location_tracker/src/model/config/background_location_tracker_config.dart';
import 'package:flutter/services.dart';

class ForegroundChannel {
  static const _FOREGROUND_CHANNEL_NAME = 'com.icapps.background_location_tracker/foreground_channel';

  static const MethodChannel _foregroundChannel = MethodChannel(_FOREGROUND_CHANNEL_NAME);

  static Future<void> initialize(Function callbackDispatcher, {BackgroundLocationTrackerConfig config}) async {
    final callback = PluginUtilities.getCallbackHandle(callbackDispatcher);
    assert(callback != null, 'The callbackDispatcher needs to be either a static function or a top level function to be accessible as a Flutter entry point.');
    final handle = callback.toRawHandle();
    await _foregroundChannel.invokeMethod<void>(
      'initialize',
      {
        'callback_handle': handle,
        'logging_enabled': config.loggingEnabled,
        'android_config_channel_name': config.androidConfig.channelName,
      },
    );
  }

  static Future<bool> isTracking() => _foregroundChannel.invokeMethod<bool>('isTracking');

  static Future<void> startTracking() => _foregroundChannel.invokeMethod('startTracking');

  static Future<void> stopTracking() => _foregroundChannel.invokeMethod('stopTracking');
}
