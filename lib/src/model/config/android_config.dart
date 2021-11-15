import 'package:flutter/foundation.dart';

/// Android configuration that will only be used by the Android implementation
///
/// Default Values:
/// - channelName: "Background Tracking"
/// - notificationBody: "Background tracking active. Tap to open."
/// - notificationIcon: null => "Uses the launcher icon"
/// - cancelTrackingActionText: "Stop Tracking."
/// - enableNotificationLocationUpdates: false
/// - enableCancelTrackingAction: true
/// - trackingInterval: 10 seconds
@immutable
class AndroidConfig {
  /// The name that will be used for the permanent notification channel.
  final String channelName;

  ///The message that will be shown in the permanent notification
  final String notificationBody;

  /// The icon string will be used to get the id of an icon that is added to you android native project
  final String? notificationIcon;

  /// enableNotificationLocationUpdates will only be used when logging is enabled
  final bool enableNotificationLocationUpdates;

  /// This string will be uses as text of the notification cancel action
  final String cancelTrackingActionText;

  /// This option will show a cancel tracking action in the notification
  final bool enableCancelTrackingAction;

  /// The interval in which location updates are requested to be delivered.
  /// This is a request to the system, not a guarantee
  /// Defaults to an update every 10 seconds
  final Duration trackingInterval;

  /// The distance in meters that should be moved before updates are sent.
  /// Defaults to no filter (null)
  final double? distanceFilterMeters;

  const AndroidConfig({
    this.channelName = 'Background Tracking',
    this.notificationBody = 'Background tracking active. Tap to open.',
    this.notificationIcon,
    this.enableNotificationLocationUpdates = false,
    this.cancelTrackingActionText = 'Stop Tracking',
    this.enableCancelTrackingAction = true,
    this.trackingInterval = const Duration(seconds: 10),
    this.distanceFilterMeters,
  });
}
