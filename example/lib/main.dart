import 'dart:async';

import 'package:background_location_tracker/background_location_tracker.dart';
import 'package:flutter/material.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';

void _onLocationUpdate() => BackgroundLocationTrackerManager.handleBackgroundUpdated((data) => Repo().update(data));

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await BackgroundLocationTrackerManager.initialize(_onLocationUpdate);
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
        body: Column(
          children: [
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
            Expanded(
              child: Center(
                child: StreamBuilder<List<BackgroundLocationUpdateData>>(
                  stream: Repo().stream,
                  builder: (context, value) {
                    if (!value.hasData || value.data.isEmpty) {
                      return const Text('Empty');
                    }
                    return ListView.builder(
                      itemCount: value.data.length,
                      itemBuilder: (context, index) {
                        final item = value.data[index];
                        return Text('Lat: ${item.lat}, Lon: ${item.lon}');
                      },
                    );
                  },
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _getTrackingStatus() async {
    isTracking = await BackgroundLocationTrackerManager.isTracking();
    setState(() {});
  }
}

class Repo {
  static Repo _instance;
  final _list = <BackgroundLocationUpdateData>[];
  final _controller = StreamController<List<BackgroundLocationUpdateData>>.broadcast();

  Stream<List<BackgroundLocationUpdateData>> get stream => _controller.stream;

  Repo._();

  factory Repo() => _instance ??= Repo._();

  void update(BackgroundLocationUpdateData data) {
    _list.add(data);
    _controller.add(_list);
    sendNotification('Location Update: Lat: ${data.lat} Lon: ${data.lon}');
  }
}

Future<void> sendNotification(String body) async {
  const settings = InitializationSettings(android: AndroidInitializationSettings('app_icon'));
  await FlutterLocalNotificationsPlugin().initialize(settings, onSelectNotification: (payload) async {});
  await FlutterLocalNotificationsPlugin().show(
    DateTime.now().hashCode,
    'Update received in Flutter',
    body,
    const NotificationDetails(
      android: AndroidNotificationDetails(
        'flutter_location_updates',
        'Location updated in flutter',
        'Location updates from flutter',
        importance: Importance.max,
        priority: Priority.high,
        showWhen: false,
      ),
    ),
  );
}
