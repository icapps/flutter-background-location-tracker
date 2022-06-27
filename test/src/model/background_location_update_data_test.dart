import 'package:background_location_tracker/background_location_tracker.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  test('Background location updatedata', () async {
    const data = BackgroundLocationUpdateData(lat: 51.45, lon: 4.5,accuracy: 0, date: '');
    expect(data.lat, 51.45);
    expect(data.lon, 4.5);
  });
}
