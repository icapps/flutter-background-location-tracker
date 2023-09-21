import 'dart:convert';

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

  Map<String, dynamic> toMap() {
    return {
      'lat': lat,
      'lon': lon,
      'horizontalAccuracy': horizontalAccuracy,
      'alt': alt,
      'verticalAccuracy': verticalAccuracy,
      'course': course,
      'courseAccuracy': courseAccuracy,
      'speed': speed,
      'speedAccuracy': speedAccuracy,
    };
  }

  factory BackgroundLocationUpdateData.fromMap(Map<String, dynamic> map) {
    return BackgroundLocationUpdateData(
      lat: map['lat'].toDouble(),
      lon: map['lon'].toDouble(),
      horizontalAccuracy: map['horizontalAccuracy'].toDouble(),
      alt: map['alt'].toDouble(),
      verticalAccuracy: map['verticalAccuracy'].toDouble(),
      course: map['course'].toDouble(),
      courseAccuracy: map['courseAccuracy'].toDouble(),
      speed: map['speed'].toDouble(),
      speedAccuracy: map['speedAccuracy'].toDouble(),
    );
  }

  String toJson() => json.encode(toMap());

  factory BackgroundLocationUpdateData.fromJson(String source) => BackgroundLocationUpdateData.fromMap(json.decode(source));

  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;

    return other is BackgroundLocationUpdateData &&
        other.lat == lat &&
        other.lon == lon &&
        other.horizontalAccuracy == horizontalAccuracy &&
        other.alt == alt &&
        other.verticalAccuracy == verticalAccuracy &&
        other.course == course &&
        other.courseAccuracy == courseAccuracy &&
        other.speed == speed &&
        other.speedAccuracy == speedAccuracy;
  }

  @override
  int get hashCode {
    return lat.hashCode ^
        lon.hashCode ^
        horizontalAccuracy.hashCode ^
        alt.hashCode ^
        verticalAccuracy.hashCode ^
        course.hashCode ^
        courseAccuracy.hashCode ^
        speed.hashCode ^
        speedAccuracy.hashCode;
  }

  @override
  String toString() {
    return 'BackgroundLocationUpdateData(lat: $lat, lon: $lon, horizontalAccuracy: $horizontalAccuracy, alt: $alt, verticalAccuracy: $verticalAccuracy, course: $course, courseAccuracy: $courseAccuracy, speed: $speed, speedAccuracy: $speedAccuracy)';
  }

  BackgroundLocationUpdateData copyWith({
    double? lat,
    double? lon,
    double? horizontalAccuracy,
    double? alt,
    double? verticalAccuracy,
    double? course,
    double? courseAccuracy,
    double? speed,
    double? speedAccuracy,
  }) {
    return BackgroundLocationUpdateData(
      lat: lat ?? this.lat,
      lon: lon ?? this.lon,
      horizontalAccuracy: horizontalAccuracy ?? this.horizontalAccuracy,
      alt: alt ?? this.alt,
      verticalAccuracy: verticalAccuracy ?? this.verticalAccuracy,
      course: course ?? this.course,
      courseAccuracy: courseAccuracy ?? this.courseAccuracy,
      speed: speed ?? this.speed,
      speedAccuracy: speedAccuracy ?? this.speedAccuracy,
    );
  }
}
