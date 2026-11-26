import 'package:background_location_tracker/background_location_tracker.dart';
import 'package:background_location_tracker/src/channel/background_channel.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  const arguments = {
    'lat': 51.45,
    'lon': 4.5,
    'alt': 42.3,
    'vertical_accuracy': 0.3,
    'horizontal_accuracy': 1.2,
    'course': 128.3,
    'course_accuracy': 14.3,
    'speed': 12.2,
    'speed_accuracy': 0.9,
    'logging_enabled': false,
  };

  Future<BackgroundLocationUpdateData> handle(
      Map<String, Object> arguments) async {
    late BackgroundLocationUpdateData result;
    await BackgroundChannel.handleLocationUpdate(
        MethodCall('onLocationUpdate', arguments),
        (data) async => result = data);
    return result;
  }

  test('Location update with time', () async {
    final time = DateTime.utc(2025, 8, 1, 12, 30, 15, 123);
    final data =
        await handle({...arguments, 'time': time.millisecondsSinceEpoch});
    expect(data.lat, 51.45);
    expect(data.time, time);
    expect(data.time!.isUtc, true);
  });

  test('Location update without time', () async {
    final data = await handle(arguments);
    expect(data.lat, 51.45);
    expect(data.time, null);
  });
}
