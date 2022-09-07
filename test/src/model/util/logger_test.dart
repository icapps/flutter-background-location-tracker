import 'package:background_location_tracker/src/util/logger.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  test('No Log', () async {
    Object? valueTest;
    BackgroundLocationTrackerLogger.printHandler = (value) {
      valueTest = value;
    };
    BackgroundLocationTrackerLogger.log('test');
    expect(valueTest, null);
  });
  test('No Log', () async {
    Object? valueTest;
    BackgroundLocationTrackerLogger.enableLogging = false;
    BackgroundLocationTrackerLogger.printHandler = (value) {
      valueTest = value;
    };
    BackgroundLocationTrackerLogger.log('test1');
    expect(valueTest, null);
  });
  test('Log', () async {
    Object? valueTest;
    BackgroundLocationTrackerLogger.enableLogging = true;
    BackgroundLocationTrackerLogger.printHandler = (value) {
      valueTest = value;
    };
    BackgroundLocationTrackerLogger.log('test2');
    expect(valueTest, 'test2');
  });
}
