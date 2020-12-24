import 'dart:async';
import 'dart:io';
import 'dart:math';

import 'package:background_location_tracker/background_location_tracker.dart';
import 'package:flutter/material.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:permission_handler/permission_handler.dart';

void _backgroundCallback() => BackgroundLocationTrackerManager.handleBackgroundUpdated((data) async => Repo().update(data));

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await BackgroundLocationTrackerManager.initialize(
    _backgroundCallback,
    config: const BackgroundLocationTrackerConfig(
      androidConfig: AndroidConfig(
        notificationIcon: 'explore',
      ),
    ),
  );
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
              if (Platform.isIOS)
                MaterialButton(
                  child: const Text('Request Notification permission'),
                  onPressed: _requestNotificationPermission,
                ),
              MaterialButton(
                child: const Text('Send notification'),
                onPressed: () => sendNotification('Hallokes'),
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

  Future<void> _requestNotificationPermission() async {
    final result = await Permission.notification.request();
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

  void update(BackgroundLocationUpdateData data) {
    final text = 'Location Update: Lat: ${data.lat} Lon: ${data.lon}';
    print(text); // ignore: avoid_print
    sendNotification(text);
  }
}

void sendNotification(String text) {
  const settings = InitializationSettings(
    android: AndroidInitializationSettings('app_icon'),
    iOS: IOSInitializationSettings(
      requestAlertPermission: false,
      requestBadgePermission: false,
      requestSoundPermission: false,
    ),
  );
  FlutterLocalNotificationsPlugin().initialize(settings, onSelectNotification: (data) async {
    print('ON CLICK $data'); // ignore: avoid_print
  });
  FlutterLocalNotificationsPlugin().show(
    Random().nextInt(9999),
    'Title',
    text,
    const NotificationDetails(
      android: AndroidNotificationDetails('test_notification', 'Test', 'Test'),
      iOS: IOSNotificationDetails(),
    ),
  );
}
