#import "BackgroundLocationTrackerPlugin.h"
#if __has_include(<background_location_tracker/background_location_tracker-Swift.h>)
#import <background_location_tracker/background_location_tracker-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "background_location_tracker-Swift.h"
#endif

@implementation BackgroundLocationTrackerPlugin

+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftBackgroundLocationTrackerPlugin registerWithRegistrar:registrar];
}

+ (void)setPluginRegistrantCallback:(FlutterPluginRegistrantCallback)callback {
    [SwiftBackgroundLocationTrackerPlugin setPluginRegistrantCallback:callback];
}

@end
