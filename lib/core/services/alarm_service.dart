import 'package:android_alarm_manager_plus/android_alarm_manager_plus.dart';
import 'package:dotnotes/core/services/notification_service.dart';

class AlarmService {
  static final AlarmService _instance = AlarmService._internal();
  factory AlarmService() => _instance;
  AlarmService._internal();

  Future<void> init() async {
    await AndroidAlarmManager.initialize();
  }

  Future<void> scheduleAlarm({
    required int id,
    required DateTime scheduledTime,
    required String title,
    required String body,
    bool repeat = false,
  }) async {
    final now = DateTime.now();
    if (scheduledTime.isBefore(now)) {
      return;
    }

    await AndroidAlarmManager.oneShotAt(
      scheduledTime,
      id,
      _alarmCallback,
      exact: true,
      wakeup: true,
      rescheduleOnReboot: true,
      params: {'title': title, 'body': body, 'id': id},
    );
  }

  Future<void> cancelAlarm(int id) async {
    await AndroidAlarmManager.cancel(id);
  }

  static void _alarmCallback(int id, Map<String, dynamic> params) {
    final title = params['title'] as String;
    final body = params['body'] as String;
    final notifId = params['id'] as int;

    NotificationService().showNotification(
      id: notifId,
      title: title,
      body: body,
    );
  }
}
