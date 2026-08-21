import '../../domain/entities/note.dart';

class NoteEntity {
  final String id;
  final String? userId;
  final String title;
  final String description;
  final int? reminderDatetime;
  final int priorityLevel;
  final bool isCompleted;
  final int createdAt;
  final int updatedAt;
  final bool isSynced;
  final bool isDeleted;

  const NoteEntity({
    required this.id,
    this.userId,
    required this.title,
    required this.description,
    this.reminderDatetime,
    required this.priorityLevel,
    required this.isCompleted,
    required this.createdAt,
    required this.updatedAt,
    required this.isSynced,
    required this.isDeleted,
  });

  factory NoteEntity.fromMap(Map<String, dynamic> map) => NoteEntity(
        id: map['id'] as String,
        userId: map['user_id'] as String?,
        title: map['title'] as String,
        description: map['description'] as String,
        reminderDatetime: map['reminder_datetime'] as int?,
        priorityLevel: (map['priority_level'] as int?) ?? 1,
        isCompleted: (map['is_completed'] as int) == 1,
        createdAt: map['created_at'] as int,
        updatedAt: map['updated_at'] as int,
        isSynced: (map['is_synced'] as int) == 1,
        isDeleted: (map['is_deleted'] as int) == 1,
      );

  Map<String, dynamic> toMap() => {
        'id': id,
        'user_id': userId,
        'title': title,
        'description': description,
        'reminder_datetime': reminderDatetime,
        'priority_level': priorityLevel,
        'is_completed': isCompleted ? 1 : 0,
        'created_at': createdAt,
        'updated_at': updatedAt,
        'is_synced': isSynced ? 1 : 0,
        'is_deleted': isDeleted ? 1 : 0,
      };

  Note toDomain() => Note(
        id: id,
        userId: userId,
        title: title,
        description: description,
        reminderDatetime: reminderDatetime != null
            ? DateTime.fromMillisecondsSinceEpoch(reminderDatetime!)
            : null,
        priorityLevel: priorityLevelFromInt(priorityLevel),
        isCompleted: isCompleted,
        createdAt: DateTime.fromMillisecondsSinceEpoch(createdAt),
        updatedAt: DateTime.fromMillisecondsSinceEpoch(updatedAt),
        isSynced: isSynced,
        isDeleted: isDeleted,
      );

  static NoteEntity fromDomain(Note note) => NoteEntity(
        id: note.id,
        userId: note.userId,
        title: note.title,
        description: note.description,
        reminderDatetime: note.reminderDatetime?.millisecondsSinceEpoch,
        priorityLevel: priorityLevelToInt(note.priorityLevel),
        isCompleted: note.isCompleted,
        createdAt: note.createdAt.millisecondsSinceEpoch,
        updatedAt: note.updatedAt.millisecondsSinceEpoch,
        isSynced: note.isSynced,
        isDeleted: note.isDeleted,
      );

  static PriorityLevel priorityLevelFromInt(int value) {
    switch (value) {
      case 2:
        return PriorityLevel.email;
      case 3:
        return PriorityLevel.alarm;
      default:
        return PriorityLevel.normal;
    }
  }

  static int priorityLevelToInt(PriorityLevel level) {
    switch (level) {
      case PriorityLevel.email:
        return 2;
      case PriorityLevel.alarm:
        return 3;
      case PriorityLevel.normal:
        return 1;
    }
  }
}