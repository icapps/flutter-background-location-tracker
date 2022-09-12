class BackgroundLocationTrackerLogger {
  static var printHandler = print;
  static var enableLogging = false;

  static void log(Object value) {
    // ignore: avoid_print
    if (enableLogging) printHandler(value);
  }
}
