import 'dart:ui';
import 'package:flutter/services.dart';

class ForegroundChannel {
  static const _FOREGROUND_CHANNEL_NAME = 'com.icapps.background_location_tracker/foreground_channel';

  static const MethodChannel _foregroundChannel = MethodChannel(_FOREGROUND_CHANNEL_NAME);

  static Future<void> initialize(Function callbackDispatcher, {bool enableLogging}) async {
    final callback = PluginUtilities.getCallbackHandle(callbackDispatcher);
    assert(callback != null, 'The callbackDispatcher needs to be either a static function or a top level function to be accessible as a Flutter entry point.');
    final handle = callback.toRawHandle();
    await _foregroundChannel.invokeMethod<void>(
      'initialize',
      {
        'callbackHandle': handle,
        'enableLogging': enableLogging,
      },
    );
  }

  static Future<void> startTracking({bool enableLogging = false}) => _foregroundChannel.invokeMethod(
        'startTracking',
        {
          'enableLogging': enableLogging,
        },
      );

  static Future<void> stopTracking({bool enableLogging = false}) => _foregroundChannel.invokeMethod(
        'stopTracking',
        {
          'enableLogging': enableLogging,
        },
      );
}
