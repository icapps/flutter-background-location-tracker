#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# Run `pod lib lint background_location_tracker.podspec' to validate before publishing.
#
Pod::Spec.new do |s|
  s.name             = 'background_location_tracker'
  s.version          = '0.0.1'
  s.summary          = 'A new Flutter plugin that allows you to track the background location for Android & iOS'
  s.description      = <<-DESC
A new Flutter plugin that allows you to track the background location for Android & iOS
                       DESC
  s.homepage         = 'http://example.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Your Company' => 'email@example.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.dependency 'Flutter'
  s.platform = :ios, '8.0'

  # Flutter.framework does not contain a i386 slice.
  s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES', 'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'i386' }
  s.swift_version = '5.0'
end
