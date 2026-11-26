import 'dart:async';
import 'dart:io';
import 'dart:math';

import 'package:background_location_tracker/background_location_tracker.dart';
import 'package:material_ui/material_ui.dart';
import 'package:flutter/services.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:shared_preferences/shared_preferences.dart';

const _initialTrackingInterval = Duration(seconds: 4);

@pragma('vm:entry-point')
void backgroundCallback() {
  BackgroundLocationTrackerManager.handleBackgroundUpdated(
    (data) async => Repo().update(data),
  );
}

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await BackgroundLocationTrackerManager.initialize(
    backgroundCallback,
    config: const BackgroundLocationTrackerConfig(
      loggingEnabled: true,
      androidConfig: AndroidConfig(
        trackingInterval: _initialTrackingInterval,
        distanceFilterMeters: null,
      ),
      iOSConfig: IOSConfig(
        activityType: ActivityType.FITNESS,
        distanceFilterMeters: null,
        restartAfterKill: true,
      ),
    ),
  );

  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Background Location Tracker',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.teal),
        useMaterial3: true,
      ),
      darkTheme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
          seedColor: Colors.teal,
          brightness: Brightness.dark,
        ),
        useMaterial3: true,
      ),
      home: const HomePage(),
    );
  }
}

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> with WidgetsBindingObserver {
  static const _intervals = [
    Duration(seconds: 1),
    _initialTrackingInterval,
    Duration(seconds: 10),
  ];

  var _isTracking = false;
  var _isBusy = false;

  /// `initialize` resets the interval, so this is correct after every app start
  var _interval = _initialTrackingInterval;
  var _locationStatus = PermissionStatus.denied;
  var _notificationStatus = PermissionStatus.denied;
  List<BackgroundLocationUpdateData> _locations = [];
  Timer? _timer;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _getTrackingStatus();
    _getPermissionStatuses();
    _timer = Timer.periodic(
        const Duration(milliseconds: 500), (_) => _getLocations());
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    _timer?.cancel();
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    // Permissions can be changed in the system settings
    if (state == AppLifecycleState.resumed) _getPermissionStatuses();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Background Location Tracker'),
        actions: [
          IconButton(
            tooltip: 'Send test notification',
            icon: const Icon(Icons.notifications_outlined),
            onPressed: () => sendNotification('Hello from another world'),
          ),
        ],
      ),
      body: CustomScrollView(
        slivers: [
          SliverPadding(
            padding: const EdgeInsets.fromLTRB(16, 8, 16, 0),
            sliver: SliverList.list(
              children: [
                _buildTrackingCard(context),
                const SizedBox(height: 12),
                _buildPermissionsCard(context),
                const SizedBox(height: 16),
                _buildLocationsHeader(context),
              ],
            ),
          ),
          if (_locations.isEmpty)
            const SliverFillRemaining(
              hasScrollBody: false,
              child: _EmptyLocations(),
            )
          else
            SliverPadding(
              padding: const EdgeInsets.only(bottom: 16),
              sliver: SliverList.builder(
                itemCount: _locations.length,
                // Newest first
                itemBuilder: (context, index) => _LocationTile(
                  data: _locations[_locations.length - 1 - index],
                ),
              ),
            ),
        ],
      ),
    );
  }

  Widget _buildTrackingCard(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final textTheme = Theme.of(context).textTheme;
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Row(
              children: [
                Icon(
                  _isTracking ? Icons.my_location : Icons.location_disabled,
                  color: _isTracking ? colors.primary : colors.outline,
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Text(
                    _isTracking ? 'Tracking' : 'Not tracking',
                    style: textTheme.titleLarge,
                  ),
                ),
                FilledButton.icon(
                  onPressed: _isBusy ? null : _toggleTracking,
                  icon: Icon(_isTracking ? Icons.stop : Icons.play_arrow),
                  label: Text(_isTracking ? 'Stop' : 'Start'),
                  style: _isTracking
                      ? FilledButton.styleFrom(
                          backgroundColor: colors.error,
                          foregroundColor: colors.onError,
                        )
                      : null,
                ),
              ],
            ),
            if (Platform.isAndroid) ...[
              const SizedBox(height: 16),
              Text('Interval', style: textTheme.labelLarge),
              const SizedBox(height: 8),
              SegmentedButton<Duration>(
                segments: [
                  for (final interval in _intervals)
                    ButtonSegment(
                      value: interval,
                      label: Text('${interval.inSeconds}s'),
                    ),
                ],
                selected: {_interval},
                onSelectionChanged: (selection) =>
                    _updateTrackingInterval(selection.single),
              ),
              const SizedBox(height: 8),
              Text(
                'Can be changed while tracking, without restarting it.',
                style: textTheme.bodySmall
                    ?.copyWith(color: colors.onSurfaceVariant),
              ),
            ],
          ],
        ),
      ),
    );
  }

  Widget _buildPermissionsCard(BuildContext context) {
    return Card(
      clipBehavior: Clip.antiAlias,
      child: Column(
        children: [
          _PermissionTile(
            icon: Icons.location_on_outlined,
            title: 'Location',
            status: _locationStatus,
            onRequest: () => _requestPermission(Permission.location),
          ),
          const Divider(height: 1),
          _PermissionTile(
            icon: Icons.notifications_outlined,
            title: 'Notifications',
            subtitle: Platform.isAndroid ? 'Needed from Android 13' : null,
            status: _notificationStatus,
            onRequest: () => _requestPermission(Permission.notification),
          ),
        ],
      ),
    );
  }

  Widget _buildLocationsHeader(BuildContext context) {
    return Row(
      children: [
        Expanded(
          child: Text(
            'Locations (${_locations.length})',
            style: Theme.of(context).textTheme.titleMedium,
          ),
        ),
        TextButton.icon(
          onPressed: _locations.isEmpty ? null : _clearLocations,
          icon: const Icon(Icons.delete_outline),
          label: const Text('Clear'),
        ),
      ],
    );
  }

  Future<void> _getTrackingStatus() async {
    final isTracking = await BackgroundLocationTrackerManager.isTracking();
    if (mounted) setState(() => _isTracking = isTracking);
  }

  Future<void> _getPermissionStatuses() async {
    final locationStatus = await Permission.location.status;
    final notificationStatus = await Permission.notification.status;
    if (!mounted) return;
    setState(() {
      _locationStatus = locationStatus;
      _notificationStatus = notificationStatus;
    });
  }

  Future<void> _requestPermission(Permission permission) async {
    final status = await permission.status;
    if (status.isPermanentlyDenied) {
      await openAppSettings();
    } else {
      await permission.request();
    }
    await _getPermissionStatuses();
  }

  Future<void> _toggleTracking() async {
    setState(() => _isBusy = true);
    try {
      if (_isTracking) {
        await BackgroundLocationTrackerManager.stopTracking();
      } else {
        await BackgroundLocationTrackerManager.startTracking();
      }
      await _getTrackingStatus();
    } finally {
      if (mounted) setState(() => _isBusy = false);
    }
  }

  Future<void> _updateTrackingInterval(Duration interval) async {
    final previous = _interval;
    setState(() => _interval = interval);
    try {
      await BackgroundLocationTrackerManager.updateTrackingInterval(interval);
    } on PlatformException catch (e) {
      if (!mounted) return;
      setState(() => _interval = previous);
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(
          content: Text('Could not change the interval: ${e.message}')));
    }
  }

  Future<void> _getLocations() async {
    final locations = await LocationDao().getLocations();
    if (mounted) setState(() => _locations = locations);
  }

  Future<void> _clearLocations() async {
    await LocationDao().clear();
    await _getLocations();
  }
}

class _PermissionTile extends StatelessWidget {
  const _PermissionTile({
    required this.icon,
    required this.title,
    required this.status,
    required this.onRequest,
    this.subtitle,
  });

  final IconData icon;
  final String title;
  final String? subtitle;
  final PermissionStatus status;
  final VoidCallback onRequest;

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final isGranted = status.isGranted || status.isLimited;
    return ListTile(
      leading: Icon(icon),
      title: Text(title),
      subtitle: subtitle == null ? null : Text(subtitle!),
      trailing: isGranted
          ? Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                Icon(Icons.check_circle, color: colors.primary, size: 20),
                const SizedBox(width: 6),
                Text('Granted', style: TextStyle(color: colors.primary)),
              ],
            )
          : OutlinedButton(
              onPressed: onRequest,
              child: Text(status.isPermanentlyDenied ? 'Settings' : 'Allow'),
            ),
    );
  }
}

class _LocationTile extends StatelessWidget {
  const _LocationTile({required this.data});

  final BackgroundLocationUpdateData data;

  @override
  Widget build(BuildContext context) {
    final time = data.time?.toLocal();
    final details = [
      if (data.horizontalAccuracy >= 0)
        '±${data.horizontalAccuracy.toStringAsFixed(0)} m',
      if (data.speed >= 0) '${(data.speed * 3.6).toStringAsFixed(1)} km/h',
    ];
    return ListTile(
      dense: true,
      leading: const Icon(Icons.place_outlined),
      title: Text(
        '${data.lat.toStringAsFixed(6)}, ${data.lon.toStringAsFixed(6)}',
        style: const TextStyle(fontFeatures: [FontFeature.tabularFigures()]),
      ),
      subtitle: details.isEmpty ? null : Text(details.join(' · ')),
      trailing: Text(
        time == null ? '' : _formatTime(time),
        style: Theme.of(context)
            .textTheme
            .bodySmall
            ?.copyWith(fontFeatures: const [FontFeature.tabularFigures()]),
      ),
    );
  }

  static String _formatTime(DateTime time) {
    String two(int value) => value.toString().padLeft(2, '0');
    final millis = time.millisecond.toString().padLeft(3, '0');
    return '${two(time.hour)}:${two(time.minute)}:${two(time.second)}.$millis';
  }
}

class _EmptyLocations extends StatelessWidget {
  const _EmptyLocations();

  @override
  Widget build(BuildContext context) {
    final color = Theme.of(context).colorScheme.outline;
    return Padding(
      padding: const EdgeInsets.all(32),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(Icons.explore_outlined, size: 48, color: color),
          const SizedBox(height: 12),
          Text(
            'No locations yet.\nStart tracking to receive updates.',
            textAlign: TextAlign.center,
            style: TextStyle(color: color),
          ),
        ],
      ),
    );
  }
}

class Repo {
  static Repo? _instance;

  Repo._();

  factory Repo() => _instance ??= Repo._();

  Future<void> update(BackgroundLocationUpdateData data) async {
    final text = 'Lat: ${data.lat} Lon: ${data.lon}';
    // ignore: avoid_print
    print('Location Update: $text Time: ${data.time?.toIso8601String()}');
    sendNotification(text);
    await LocationDao().saveLocation(data);
  }
}

class LocationDao {
  static const _locationsKey = 'background_updated_locations_json';

  static LocationDao? _instance;

  LocationDao._();

  factory LocationDao() => _instance ??= LocationDao._();

  SharedPreferences? _prefs;

  Future<void> _lastSave = Future.value();

  Future<SharedPreferences> get prefs async =>
      _prefs ??= await SharedPreferences.getInstance();

  /// Multiple locations can be received at once, so they are saved one by one
  Future<void> saveLocation(BackgroundLocationUpdateData data) => _lastSave =
          _lastSave.then((_) => _saveLocation(data)).catchError((Object e) {
        print('Failed to save the location: $e'); // ignore: avoid_print
      });

  Future<void> _saveLocation(BackgroundLocationUpdateData data) async {
    final prefs = await this.prefs;
    await prefs.reload();
    final locations = prefs.getStringList(_locationsKey) ?? [];
    locations.add(data.toJson());
    await prefs.setStringList(_locationsKey, locations);
  }

  Future<List<BackgroundLocationUpdateData>> getLocations() async {
    final prefs = await this.prefs;
    await prefs.reload();
    final locations = prefs.getStringList(_locationsKey) ?? [];
    return locations.map(BackgroundLocationUpdateData.fromJson).toList();
  }

  Future<void> clear() async => (await prefs).remove(_locationsKey);
}

final _notifications = FlutterLocalNotificationsPlugin();
Future<bool?>? _notificationsInitialized;

Future<void> sendNotification(String text) async {
  try {
    await (_notificationsInitialized ??= _notifications.initialize(
      const InitializationSettings(
        android: AndroidInitializationSettings('@mipmap/ic_launcher'),
        iOS: DarwinInitializationSettings(
          requestAlertPermission: false,
          requestBadgePermission: false,
          requestSoundPermission: false,
        ),
      ),
    ));
    await _notifications.show(
      Random().nextInt(9999),
      'Location update',
      text,
      const NotificationDetails(
        android: AndroidNotificationDetails('test_notification', 'Test'),
        iOS: DarwinNotificationDetails(),
      ),
    );
  } catch (e) {
    print('Failed to show a notification: $e'); // ignore: avoid_print
  }
}
