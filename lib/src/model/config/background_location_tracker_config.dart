import 'package:background_location_tracker/src/model/config/android_config.dart';

/// BackgroundLocationTrackerConfig will be used to setup the plugin
///
/// Default Values:
/// - loggingEnabled: false
/// - androidConfig: AndroidConfig()
class BackgroundLocationTrackerConfig {

  /// loggingEnabled will be used for dart & native logs.
  final bool loggingEnabled;

  /// androidConfig will only be used by the android implementation.
  /// By default only
  final AndroidConfig androidConfig;

  const BackgroundLocationTrackerConfig({
    this.loggingEnabled = false,
    this.androidConfig = const AndroidConfig(),
  });
}
