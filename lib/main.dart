import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:permission_handler/permission_handler.dart';
import 'app/app.dart';
import 'features/reminder/notification_service.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // Request notification permission (Android 13+)
  await _requestPermissions();

  await NotificationService().initialize();
  await NotificationService().rescheduleAllNotifications();

  runApp(const ProviderScope(child: App()));
}

Future<void> _requestPermissions() async {
  // Android 13+ (API 33) needs runtime permission for notifications
  if (await Permission.notification.isDenied) {
    await Permission.notification.request();
  }

  // Android 12+ (API 31) needs exact alarm permission
  if (await Permission.scheduleExactAlarm.isDenied) {
    await Permission.scheduleExactAlarm.request();
  }
}