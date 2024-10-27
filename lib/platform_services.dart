import 'dart:developer';
import 'package:flutter/services.dart';

class PlatformService {
  static const MethodChannel _channel = MethodChannel('platform_channel');

  static Future<void> showToast(String message) async {
    try {
      await _channel.invokeMethod('showToast', {'message': message});
    } catch (e) {
      log(e.toString());
    }
  }

  static Future<String> getBatteryLevel() async {
    String batteryLevel;
    try {
      final result = await _channel.invokeMethod<int>('getBatteryLevel');
      batteryLevel = 'Battery level at $result % .';
    } on PlatformException catch (e) {
      batteryLevel = "Failed to get battery level: '${e.message}'.";
    }

    return batteryLevel;
  }
}
