import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:timezone/data/latest.dart' as tz;
import 'package:timezone/timezone.dart' as tz;
import '../../../domain/entities/note.dart';
import '../../../data/local/notes_database.dart';

class NotificationService {
  static final NotificationService _instance = NotificationService._();
  factory NotificationService() => _instance;
  NotificationService._();

  final FlutterLocalNotificationsPlugin _notifications = FlutterLocalNotificationsPlugin();
  bool _initialized = false;

  Future<void> initialize() async {
    if (_initialized) return;
    tz.initializeTimeZones();
    const androidSettings = AndroidInitializationSettings('@mipmap/ic_launcher');
    const initSettings = InitializationSettings(android: androidSettings);
    await _notifications.initialize(initSettings, onDidReceiveNotificationResponse: _onNotificationTapped);
    _initialized = true;
  }

  void _onNotificationTapped(NotificationResponse response) {}

  Future<void> scheduleNotification(Note note) async {
    if (note.reminderDatetime == null) return;
    if (note.reminderDatetime!.isBefore(DateTime.now())) return;

    final notificationId = note.id.hashCode.abs() % 100000;
    final scheduledDate = tz.TZDateTime.from(note.reminderDatetime!, tz.local);

    const androidDetails = AndroidNotificationDetails(
      'reminder_channel',
      'Reminders',
      channelDescription: 'Notification reminders for notes',
      importance: Importance.high,
      priority: Priority.high,
    );
    const details = NotificationDetails(android: androidDetails);

    await _notifications.zonedSchedule(
      notificationId,
      note.title,
      _buildBody(note),
      scheduledDate,
      details,
      androidScheduleMode: AndroidScheduleMode.inexactAllowWhileIdle,
      uiLocalNotificationDateInterpretation: UILocalNotificationDateInterpretation.absoluteTime,
    );
  }

  String _buildBody(Note note) {
    switch (note.priorityLevel) {
      case PriorityLevel.alarm:
        return 'Alarm! ${note.description.isEmpty ? note.title : note.description}';
      case PriorityLevel.email:
        return 'Email + Notif: ${note.description.isEmpty ? note.title : note.description}';
      case PriorityLevel.normal:
        return note.description.isEmpty ? 'Time to check your note' : note.description;
    }
  }

  Future<void> cancelNotification(String noteId) async {
    final notificationId = noteId.hashCode.abs() % 100000;
    await _notifications.cancel(notificationId);
  }

  Future<void> rescheduleAllNotifications() async {
    final db = NotesDatabase.instance;
    final entities = await db.getAll();
    for (final entity in entities) {
      final note = entity.toDomain();
      if (note.reminderDatetime != null && !note.reminderDatetime!.isBefore(DateTime.now())) {
        await scheduleNotification(note);
      }
    }
  }
}