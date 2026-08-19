import 'package:hive/hive.dart';

part 'reminder.g.dart';

@HiveType(typeId: 1)
class Reminder extends HiveObject {
  @HiveField(0)
  String id;

  @HiveField(1)
  String noteId;

  @HiveField(2)
  DateTime scheduledAt;

  @HiveField(3)
  Priority priority;

  @HiveField(4)
  RepeatType repeat;

  @HiveField(5)
  bool alarmEnabled;

  @HiveField(6)
  bool gmailEnabled;

  @HiveField(7)
  bool calendarEnabled;

  @HiveField(8)
  String? gmailRecipient;

  @HiveField(9)
  String? calendarEventId;

  @HiveField(10)
  ReminderStatus status;

  Reminder({
    required this.id,
    required this.noteId,
    required this.scheduledAt,
    this.priority = Priority.normal,
    this.repeat = RepeatType.once,
    this.alarmEnabled = true,
    this.gmailEnabled = false,
    this.calendarEnabled = false,
    this.gmailRecipient,
    this.calendarEventId,
    this.status = ReminderStatus.pending,
  });

  Reminder.empty()
      : id = '',
        noteId = '',
        scheduledAt = DateTime.now(),
        priority = Priority.normal,
        repeat = RepeatType.once,
        alarmEnabled = true,
        gmailEnabled = false,
        calendarEnabled = false,
        gmailRecipient = null,
        calendarEventId = null,
        status = ReminderStatus.pending;

  Reminder copyWith({
    String? id,
    String? noteId,
    DateTime? scheduledAt,
    Priority? priority,
    RepeatType? repeat,
    bool? alarmEnabled,
    bool? gmailEnabled,
    bool? calendarEnabled,
    String? gmailRecipient,
    String? calendarEventId,
    ReminderStatus? status,
  }) {
    return Reminder(
      id: id ?? this.id,
      noteId: noteId ?? this.noteId,
      scheduledAt: scheduledAt ?? this.scheduledAt,
      priority: priority ?? this.priority,
      repeat: repeat ?? this.repeat,
      alarmEnabled: alarmEnabled ?? this.alarmEnabled,
      gmailEnabled: gmailEnabled ?? this.gmailEnabled,
      calendarEnabled: calendarEnabled ?? this.calendarEnabled,
      gmailRecipient: gmailRecipient ?? this.gmailRecipient,
      calendarEventId: calendarEventId ?? this.calendarEventId,
      status: status ?? this.status,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'noteId': noteId,
      'scheduledAt': scheduledAt.toIso8601String(),
      'priority': priority.index,
      'repeat': repeat.index,
      'alarmEnabled': alarmEnabled,
      'gmailEnabled': gmailEnabled,
      'calendarEnabled': calendarEnabled,
      'gmailRecipient': gmailRecipient,
      'calendarEventId': calendarEventId,
      'status': status.index,
    };
  }

  factory Reminder.fromJson(Map<String, dynamic> json) {
    return Reminder(
      id: json['id'],
      noteId: json['noteId'],
      scheduledAt: DateTime.parse(json['scheduledAt']),
      priority: Priority.values[json['priority']],
      repeat: RepeatType.values[json['repeat']],
      alarmEnabled: json['alarmEnabled'],
      gmailEnabled: json['gmailEnabled'],
      calendarEnabled: json['calendarEnabled'],
      gmailRecipient: json['gmailRecipient'],
      calendarEventId: json['calendarEventId'],
      status: ReminderStatus.values[json['status']],
    );
  }
}

enum Priority { low, normal, high, urgent }

enum RepeatType { once, daily, weekly, monthly }

enum ReminderStatus { pending, completed, snoozed }
