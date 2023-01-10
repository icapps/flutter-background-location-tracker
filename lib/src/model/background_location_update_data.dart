/// BackgroundLocationUpdateData will contain all the data that is send when getting a background location update
///
/// latitude & longitude
class BackgroundLocationUpdateData {
  /// Latitude
  final double lat;

  /// Longitude
  final double lon;

  /// The radius of uncertainty for the location, measured in meters. Negative value if not available.
  final double horizontalAccuracy;

  /// Altitude
  final double alt;

  /// The validity of the altitude values, and their estimated uncertainty, measured in meters. Negative value if not available.
  final double verticalAccuracy;

  /// Direction in which the device is traveling, measured in degrees and relative to due north. Negative value if not available.
  final double course;

  /// The accuracy of the course value, measured in degrees. Negative value if not available.
  final double courseAccuracy;

  /// Instantaneous speed of the device, measured in meters per second. Negative value if not available.
  final double speed;

  /// The accuracy of the speed value, measured in meters per second. Negative value if not available.
  final double speedAccuracy;

  const BackgroundLocationUpdateData({
    required this.lat,
    required this.lon,
    required this.horizontalAccuracy,
    required this.alt,
    required this.verticalAccuracy,
    required this.course,
    required this.courseAccuracy,
    required this.speed,
    required this.speedAccuracy,
  });
}
