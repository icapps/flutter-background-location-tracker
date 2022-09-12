import 'package:background_location_tracker/background_location_tracker.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  test('iOS Config default', () async {
    const config = IOSConfig();
    expect(config.activityType, ActivityType.AUTOMOTIVE);
    expect(config.distanceFilterMeters, null);
    expect(config.restartAfterKill, false);
  });
}
