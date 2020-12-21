import 'package:flutter/widgets.dart';

/// BackgroundLocationUpdateData will contain all the data that is send when getting a background location update
///
/// latitude & longitude
class BackgroundLocationUpdateData {
  final double lat;
  final double lon;

  const BackgroundLocationUpdateData({
    @required this.lat,
    @required this.lon,
  });
}
