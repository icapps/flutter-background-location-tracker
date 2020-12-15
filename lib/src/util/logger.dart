class BackgroundLocationTrackerLogger {
  static var enableLogging = false;

  static void log(Object value) {
    // ignore: avoid_print
    if (enableLogging) print(value);
  }
}
