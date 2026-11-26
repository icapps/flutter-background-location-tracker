import 'package:background_location_tracker/background_location_tracker.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  test('Background location update data', () async {
    const data = BackgroundLocationUpdateData(
      lat: 51.45,
      lon: 4.5,
      horizontalAccuracy: 1.2,
      alt: 42.3,
      verticalAccuracy: 0.3,
      course: 128.3,
      courseAccuracy: 14.3,
      speed: 12.2,
      speedAccuracy: 0.9,
    );
    expect(data.lat, 51.45);
    expect(data.lon, 4.5);
    expect(data.horizontalAccuracy, 1.2);
    expect(data.alt, 42.3);
    expect(data.verticalAccuracy, 0.3);
    expect(data.course, 128.3);
    expect(data.courseAccuracy, 14.3);
    expect(data.speed, 12.2);
    expect(data.speedAccuracy, 0.9);
  });

  test('Background location update data methods', () async {
    const data = BackgroundLocationUpdateData(
      lat: 51.45,
      lon: 4.5,
      horizontalAccuracy: 1.2,
      alt: 42.3,
      verticalAccuracy: 0.3,
      course: 128.3,
      courseAccuracy: 14.3,
      speed: 12.2,
      speedAccuracy: 0.9,
    );
    final json = data.toJson();
    final data2 = BackgroundLocationUpdateData.fromJson(json);
    expect(data2.lat, 51.45);
    expect(data2.lon, 4.5);
    expect(data2.horizontalAccuracy, 1.2);
    expect(data2.alt, 42.3);
    expect(data2.verticalAccuracy, 0.3);
    expect(data2.course, 128.3);
    expect(data2.courseAccuracy, 14.3);
    expect(data2.speed, 12.2);
    expect(data2.speedAccuracy, 0.9);
    expect(data == data2, true);

    final data3 = data.copyWith(lat: 51.46);
    expect(data3.lat, 51.46);
    expect(data2.lon, 4.5);
    expect(data2.horizontalAccuracy, 1.2);
    expect(data2.alt, 42.3);
    expect(data2.verticalAccuracy, 0.3);
    expect(data2.course, 128.3);
    expect(data2.courseAccuracy, 14.3);
    expect(data2.speed, 12.2);
    expect(data2.speedAccuracy, 0.9);
    expect(data == data3, false);

    expect(data.toString(),
        'BackgroundLocationUpdateData(lat: 51.45, lon: 4.5, horizontalAccuracy: 1.2, alt: 42.3, verticalAccuracy: 0.3, course: 128.3, courseAccuracy: 14.3, speed: 12.2, speedAccuracy: 0.9, time: null)');

    expect(data.hashCode == data2.hashCode, true);
    expect(data.hashCode == data3.hashCode, false);
  });

  group('time', () {
    final time = DateTime.utc(2025, 8, 1, 12, 30, 15, 123);
    final data = BackgroundLocationUpdateData(
      lat: 51.45,
      lon: 4.5,
      horizontalAccuracy: 1.2,
      alt: 42.3,
      verticalAccuracy: 0.3,
      course: 128.3,
      courseAccuracy: 14.3,
      speed: 12.2,
      speedAccuracy: 0.9,
      time: time,
    );

    test('is saved as epoch milliseconds and restored in UTC', () {
      expect(data.toMap()['time'], time.millisecondsSinceEpoch);
      final restored = BackgroundLocationUpdateData.fromJson(data.toJson());
      expect(restored.time, time);
      expect(restored.time!.isUtc, true);
      expect(restored, data);
      expect(restored.hashCode, data.hashCode);
    });

    test('is optional in stored data', () {
      final map = data.toMap()..remove('time');
      final restored = BackgroundLocationUpdateData.fromMap(map);
      expect(restored.time, null);
      expect(restored == data, false);
    });

    test('copyWith, toString', () {
      final otherTime = time.add(const Duration(milliseconds: 1));
      expect(data.copyWith(lat: 1).time, time);
      expect(data.copyWith(time: otherTime).time, otherTime);
      expect(data.copyWith(time: otherTime) == data, false);
      expect(data.toString(), endsWith('time: 2025-08-01 12:30:15.123Z)'));
    });
  });
}
