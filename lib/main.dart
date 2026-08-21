import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'app/app.dart';
import 'features/reminder/notification_service.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  await NotificationService().initialize();
  await _requestPermissions();
  await NotificationService().rescheduleAllNotifications();

  runApp(const ProviderScope(child: App()));
}

Future<void> _requestPermissions() async {
  final plugin = FlutterLocalNotificationsPlugin();
  final android = plugin.resolvePlatformSpecificImplementation<AndroidFlutterLocalNotificationsPlugin>();
  if (android != null) {
    await android.requestNotificationsPermission();
    await android.requestExactAlarmsPermission();
  }
}