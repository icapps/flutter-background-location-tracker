/// BackgroundLocationUpdateData will contain all the data that is send when getting a background location update
///
/// latitude & longitude
class BackgroundLocationUpdateData {
  final double lat;
  final double lon;
  final double accuracy;
  final String date;

  const BackgroundLocationUpdateData({
    required this.lat,
    required this.lon,
    required this.accuracy,
    required this.date,
  });
}
