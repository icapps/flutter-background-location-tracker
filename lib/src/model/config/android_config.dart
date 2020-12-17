/// Android configuration that will only be used by the Android implementation
///
/// Default Values:
/// - channelName: "Background Tracking"
/// - notificationBody: "Background tracking active. Tap to open."
/// - cancelTrackingActionText: "Stop Tracking."
/// - enableNotificationLocationUpdates: false
/// - enableCancelTrackingAction: true
class AndroidConfig {
  // The name that will be used for the permanent notification channel.
  final String channelName;

  ///The message that will be shown in the permanent notification
  final String notificationBody;

  /// enableNotificationLocationUpdates will only be used when logging is enabled
  final bool enableNotificationLocationUpdates;

  /// This string will be uses as text of the notification cancel action
  final String cancelTrackingActionText;

  /// This option will show a cancel tracking action in the notification
  final bool enableCancelTrackingAction;

  const AndroidConfig({
    this.channelName = 'Background Tracking',
    this.notificationBody = 'Background tracking active. Tap to open.',
    this.enableNotificationLocationUpdates = false,
    this.cancelTrackingActionText = 'Stop Tracking',
    this.enableCancelTrackingAction = true,
  });
}
