import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'app/app.dart';
import 'data/remote/google/google_auth_service.dart';
import 'features/reminder/alarm_service.dart';
import 'features/reminder/notification_service.dart';
import 'features/reminder/reminder_sync_service.dart';
import 'features/settings/application/settings_notifier.dart';
import 'features/sync/application/sync_notifier.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();

  final sharedPreferences = await SharedPreferences.getInstance();
  final notificationService = NotificationService();
  await notificationService.initialize();

  final alarmService = AlarmService();
  await alarmService.initialize();

  final googleAuthService = GoogleAuthService();

  final container = ProviderScope(
    overrides: [
      sharedPreferencesProvider.overrideWithValue(sharedPreferences),
      notificationServiceProvider.overrideWithValue(notificationService),
      alarmServiceProvider.overrideWithValue(alarmService),
      googleAuthServiceProvider.overrideWithValue(googleAuthService),
    ],
    child: const DotNotesApp(),
  );

  runApp(container);

  // Background startup tasks (Reschedule reminders & silent sync)
  final backgroundContainer = ProviderContainer(
    overrides: [
      sharedPreferencesProvider.overrideWithValue(sharedPreferences),
      notificationServiceProvider.overrideWithValue(notificationService),
      alarmServiceProvider.overrideWithValue(alarmService),
      googleAuthServiceProvider.overrideWithValue(googleAuthService),
    ],
  );

  try {
    // 1. Reschedule reminders
    await backgroundContainer
        .read(reminderSyncServiceProvider)
        .rescheduleAllActiveReminders();

    // 2. Silent Cloud Sync if logged in
    final account = await googleAuthService.signInSilently();
    if (account != null) {
      await backgroundContainer.read(syncNotifierProvider.notifier).sync();
    }
  } catch (e) {
    debugPrint('Startup background tasks error: $e');
  } finally {
    backgroundContainer.dispose();
  }
}
