enum PriorityLevel {
  normal,
  email,
  alarm,
}

class NoteEntity {
  final String id;
  final String title;
  final String description;
  final DateTime? reminderDateTime;
  final PriorityLevel priorityLevel;
  final String? googleCalendarEventId;
  final bool isCompleted;
  final bool isSynced;
  final DateTime? deletedAt;
  final DateTime createdAt;
  final DateTime updatedAt;

  const NoteEntity({
    required this.id,
    required this.title,
    required this.description,
    this.reminderDateTime,
    this.priorityLevel = PriorityLevel.normal,
    this.googleCalendarEventId,
    this.isCompleted = false,
    this.isSynced = false,
    this.deletedAt,
    required this.createdAt,
    required this.updatedAt,
  });

  NoteEntity copyWith({
    String? id,
    String? title,
    String? description,
    DateTime? Function()? reminderDateTime,
    PriorityLevel? priorityLevel,
    String? Function()? googleCalendarEventId,
    bool? isCompleted,
    bool? isSynced,
    DateTime? Function()? deletedAt,
    DateTime? createdAt,
    DateTime? updatedAt,
  }) {
    return NoteEntity(
      id: id ?? this.id,
      title: title ?? this.title,
      description: description ?? this.description,
      reminderDateTime: reminderDateTime != null
          ? reminderDateTime()
          : this.reminderDateTime,
      priorityLevel: priorityLevel ?? this.priorityLevel,
      googleCalendarEventId: googleCalendarEventId != null
          ? googleCalendarEventId()
          : this.googleCalendarEventId,
      isCompleted: isCompleted ?? this.isCompleted,
      isSynced: isSynced ?? this.isSynced,
      deletedAt: deletedAt != null ? deletedAt() : this.deletedAt,
      createdAt: createdAt ?? this.createdAt,
      updatedAt: updatedAt ?? this.updatedAt,
    );
  }
}
