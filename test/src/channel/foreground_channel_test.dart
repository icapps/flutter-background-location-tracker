import 'package:background_location_tracker/background_location_tracker.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  const channel = MethodChannel(
      'com.icapps.background_location_tracker/foreground_channel');
  final messenger =
      TestWidgetsFlutterBinding.ensureInitialized().defaultBinaryMessenger;
  final calls = <MethodCall>[];

  setUp(() {
    calls.clear();
    messenger.setMockMethodCallHandler(channel, (call) async {
      calls.add(call);
      return true;
    });
  });

  tearDown(() => messenger.setMockMethodCallHandler(channel, null));

  test('updateTrackingInterval sends the interval in milliseconds', () async {
    await BackgroundLocationTrackerManager.updateTrackingInterval(
        const Duration(seconds: 1, milliseconds: 500));
    expect(calls.single.method, 'updateTrackingInterval');
    expect(calls.single.arguments, {'android_update_interval_msec': 1500});
  });

  test('updateTrackingInterval does not accept invalid intervals', () async {
    for (final interval in const [
      Duration.zero,
      Duration(seconds: -1),
      Duration(microseconds: 999),
    ]) {
      await expectLater(
          BackgroundLocationTrackerManager.updateTrackingInterval(interval),
          throwsArgumentError);
    }
    expect(calls, isEmpty);
  });

  test('updateTrackingInterval reports platform errors', () async {
    messenger.setMockMethodCallHandler(channel,
        (call) async => throw PlatformException(code: 'permission_denied'));
    await expectLater(
        BackgroundLocationTrackerManager.updateTrackingInterval(
            const Duration(seconds: 1)),
        throwsA(isA<PlatformException>()
            .having((e) => e.code, 'code', 'permission_denied')));
  });
}
