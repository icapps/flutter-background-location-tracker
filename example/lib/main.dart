import 'dart:async';

import 'package:background_location_tracker/background_location_tracker.dart';
import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';

void _backgroundCallback() => BackgroundLocationTrackerManager.handleBackgroundUpdated((data) => Repo().update(data));

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await BackgroundLocationTrackerManager.initialize(_backgroundCallback);
  runApp(MyApp());
}

@override
class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  var isTracking = false;

  @override
  void initState() {
    super.initState();
    _getTrackingStatus();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Container(
          width: double.infinity,
          child: Column(
            children: [
              MaterialButton(
                child: const Text('Request location permission'),
                onPressed: _requestLocationPermission,
              ),
              if (isTracking != null) ...[
                MaterialButton(
                  child: const Text('Start Tracking'),
                  onPressed: isTracking
                      ? null
                      : () {
                          BackgroundLocationTrackerManager.startTracking();
                          setState(() => isTracking = true);
                        },
                ),
                MaterialButton(
                  child: const Text('Stop Tracking'),
                  onPressed: isTracking
                      ? () {
                          BackgroundLocationTrackerManager.stopTracking();
                          setState(() => isTracking = false);
                        }
                      : null,
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }

  Future<void> _getTrackingStatus() async {
    isTracking = await BackgroundLocationTrackerManager.isTracking();
    setState(() {});
  }

  Future<void> _requestLocationPermission() async {
    final result = await Permission.locationAlways.request();
    if (result == PermissionStatus.granted) {
      print('GRANTED'); // ignore: avoid_print
    } else {
      print('NOT GRANTED'); // ignore: avoid_print
    }
  }
}

class Repo {
  static Repo _instance;

  Repo._();

  factory Repo() => _instance ??= Repo._();

  void update(BackgroundLocationUpdateData data) => print('Location Update: Lat: ${data.lat} Lon: ${data.lon}');
}
