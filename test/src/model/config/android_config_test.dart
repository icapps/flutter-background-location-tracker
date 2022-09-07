import 'package:background_location_tracker/background_location_tracker.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  test('Android Config default', () async {
    const config = AndroidConfig();
    expect(config.channelName, 'Background Tracking');
    expect(config.notificationBody, 'Background tracking active. Tap to open.');
    expect(config.notificationIcon, null);
    expect(config.enableNotificationLocationUpdates, false);
    expect(config.cancelTrackingActionText, 'Stop Tracking');
    expect(config.enableCancelTrackingAction, true);
    expect(config.trackingInterval, const Duration(seconds: 10));
    expect(config.distanceFilterMeters, null);
  });
}
