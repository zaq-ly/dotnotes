import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import '../../domain/entities/note.dart';

class NotificationService {
  static final NotificationService _instance = NotificationService._();
  factory NotificationService() => _instance;
  NotificationService._();

  final FlutterLocalNotificationsPlugin _notifications = FlutterLocalNotificationsPlugin();

  Future<void> initialize() async {
    const androidSettings = AndroidInitializationSettings('@mipmap/ic_launcher');
    const initSettings = InitializationSettings(android: androidSettings);
    await _notifications.initialize(initSettings);
  }

  Future<void> scheduleNotification(Note note) async {
    if (note.reminderDatetime == null) return;

    final notificationId = note.id.hashCode.abs() % 1000;

    const androidDetails = AndroidNotificationDetails(
      'reminder_channel',
      'Reminders',
      channelDescription: 'Notification reminders for notes',
      importance: Importance.high,
      priority: Priority.high,
    );
    const details = NotificationDetails(android: androidDetails);

    await _notifications.show(
      notificationId,
      note.title,
      'Reminder',
      details,
    );
  }

  Future<void> cancelNotification(String noteId) async {
    final notificationId = noteId.hashCode.abs() % 1000;
    await _notifications.cancel(notificationId);
  }

  Future<void> rescheduleAllNotifications(List<Note> notes) async {
    for (final note in notes) {
      if (note.reminderDatetime != null && !note.reminderDatetime!.isBefore(DateTime.now())) {
        await scheduleNotification(note);
      }
    }
  }
}