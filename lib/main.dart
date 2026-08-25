import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'app/app.dart';
import 'features/reminder/alarm_service.dart';
import 'features/reminder/notification_service.dart';
import 'features/reminder/reminder_sync_service.dart';
import 'features/settings/application/settings_notifier.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();

  final sharedPreferences = await SharedPreferences.getInstance();
  final notificationService = NotificationService();
  await notificationService.initialize();

  final alarmService = AlarmService();
  await alarmService.initialize();

  final container = ProviderScope(
    overrides: [
      sharedPreferencesProvider.overrideWithValue(sharedPreferences),
      notificationServiceProvider.overrideWithValue(notificationService),
      alarmServiceProvider.overrideWithValue(alarmService),
    ],
    child: const DotNotesApp(),
  );

  runApp(container);

  // Reschedule active reminders in background on startup
  final element = ProviderContainer(
    overrides: [
      sharedPreferencesProvider.overrideWithValue(sharedPreferences),
      notificationServiceProvider.overrideWithValue(notificationService),
      alarmServiceProvider.overrideWithValue(alarmService),
    ],
  );
  try {
    await element.read(reminderSyncServiceProvider).rescheduleAllActiveReminders();
  } catch (e) {
    debugPrint('Reminder reschedule error: $e');
  } finally {
    element.dispose();
  }
}
