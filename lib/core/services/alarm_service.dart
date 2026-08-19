import 'package:dotnotes/core/services/notification_service.dart';

class AlarmService {
  static final AlarmService _instance = AlarmService._internal();
  factory AlarmService() => _instance;
  AlarmService._internal();

  Future<void> init() async {
    await NotificationService().init();
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

    await NotificationService().scheduleNotification(
      id: id,
      title: title,
      body: body,
      scheduledTime: scheduledTime,
    );
  }

  Future<void> cancelAlarm(int id) async {
    await NotificationService().cancelNotification(id);
  }
}
