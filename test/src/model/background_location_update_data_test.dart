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
}
