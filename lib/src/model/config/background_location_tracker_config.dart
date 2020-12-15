import 'package:background_location_tracker/src/model/config/android_config.dart';

class BackgroundLocationTrackerConfig {
  final bool loggingEnabled;
  final AndroidConfig androidConfig;

  const BackgroundLocationTrackerConfig({
    this.loggingEnabled = false,
    this.androidConfig = const AndroidConfig(),
  });
}
