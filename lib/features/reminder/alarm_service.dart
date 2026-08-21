import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import '../../domain/entities/note.dart';

class AlarmService {
  static final AlarmService _instance = AlarmService._();
  factory AlarmService() => _instance;
  AlarmService._();

  final FlutterLocalNotificationsPlugin _notifications = FlutterLocalNotificationsPlugin();

  Future<void> initialize() async {
    const androidSettings = AndroidInitializationSettings('@mipmap/ic_launcher');
    const initSettings = InitializationSettings(android: androidSettings);
    await _notifications.initialize(initSettings);
  }

  Future<void> scheduleAlarm(Note note) async {
    if (note.reminderDatetime == null) return;
    if (note.priorityLevel != PriorityLevel.alarm) return;

    final alarmId = note.id.hashCode.abs() % 1000 + 5000;

    const androidDetails = AndroidNotificationDetails(
      'alarm_channel',
      'Alarms',
      channelDescription: 'Alarm notifications',
      importance: Importance.max,
      priority: Priority.high,
      fullScreenIntent: true,
    );
    const details = NotificationDetails(android: androidDetails);

    await _notifications.show(alarmId, 'Alarm!', note.title, details);
  }

  Future<void> cancelAlarm(String noteId) async {
    final alarmId = noteId.hashCode.abs() % 1000 + 5000;
    await _notifications.cancel(alarmId);
  }

  Future<void> rescheduleAllAlarms(List<Note> notes) async {
    for (final note in notes) {
      if (note.reminderDatetime != null &&
          note.priorityLevel == PriorityLevel.alarm &&
          !note.reminderDatetime!.isBefore(DateTime.now())) {
        await scheduleAlarm(note);
      }
    }
  }
}