import 'dart:io';
import 'package:android_alarm_manager_plus/android_alarm_manager_plus.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

class AlarmService {
  bool _isInitialized = false;

  Future<void> initialize() async {
    if (_isInitialized) return;
    if (!kIsWeb && Platform.isAndroid) {
      await AndroidAlarmManager.initialize();
      _isInitialized = true;
    }
  }

  int noteIdToAlarmId(String noteId) {
    return (noteId.hashCode.abs() % 2147483647);
  }

  @pragma('vm:entry-point')
  static void alarmCallback(int id) {
    debugPrint('[AlarmService] Native alarm triggered for id: $id');
  }

  Future<bool> scheduleAlarm({
    required String noteId,
    required DateTime scheduledDate,
    required String title,
    required String description,
  }) async {
    if (kIsWeb || !Platform.isAndroid) return false;
    if (scheduledDate.isBefore(DateTime.now())) return false;

    final alarmId = noteIdToAlarmId(noteId);

    return AndroidAlarmManager.oneShotAt(
      scheduledDate,
      alarmId,
      alarmCallback,
      exact: true,
      wakeup: true,
      rescheduleOnReboot: true,
      alarmClock: true,
    );
  }

  Future<bool> cancelAlarm(String noteId) async {
    if (kIsWeb || !Platform.isAndroid) return false;
    final alarmId = noteIdToAlarmId(noteId);
    return AndroidAlarmManager.cancel(alarmId);
  }
}

final alarmServiceProvider = Provider<AlarmService>((ref) {
  return AlarmService();
});
