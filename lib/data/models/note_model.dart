import 'package:drift/drift.dart';
import '../../domain/entities/note.dart';
import '../local/drift/database.dart';

abstract class NoteModel {
  static NoteEntity fromDrift(NotesTableData data) {
    return NoteEntity(
      id: data.id,
      title: data.title,
      description: data.description,
      reminderDateTime: data.reminderDateTime,
      priorityLevel: _intToPriority(data.priorityLevel),
      googleCalendarEventId: data.googleCalendarEventId,
      isCompleted: data.isCompleted,
      isSynced: data.isSynced,
      deletedAt: data.deletedAt,
      createdAt: data.createdAt,
      updatedAt: data.updatedAt,
    );
  }

  static NotesTableCompanion toCompanion(NoteEntity entity) {
    return NotesTableCompanion(
      id: Value(entity.id),
      title: Value(entity.title),
      description: Value(entity.description),
      reminderDateTime: Value(entity.reminderDateTime),
      priorityLevel: Value(_priorityToInt(entity.priorityLevel)),
      googleCalendarEventId: Value(entity.googleCalendarEventId),
      isCompleted: Value(entity.isCompleted),
      isSynced: Value(entity.isSynced),
      deletedAt: Value(entity.deletedAt),
      createdAt: Value(entity.createdAt),
      updatedAt: Value(entity.updatedAt),
    );
  }

  static PriorityLevel _intToPriority(int value) {
    switch (value) {
      case 1:
        return PriorityLevel.email;
      case 2:
        return PriorityLevel.alarm;
      case 0:
      default:
        return PriorityLevel.normal;
    }
  }

  static int _priorityToInt(PriorityLevel priority) {
    switch (priority) {
      case PriorityLevel.email:
        return 1;
      case PriorityLevel.alarm:
        return 2;
      case PriorityLevel.normal:
        return 0;
    }
  }

  // JSON serialization for Google Drive sync
  static Map<String, dynamic> toJson(NoteEntity entity) {
    return {
      'id': entity.id,
      'title': entity.title,
      'description': entity.description,
      'reminder_datetime': entity.reminderDateTime?.toIso8601String(),
      'priority_level': _priorityToInt(entity.priorityLevel),
      'google_calendar_event_id': entity.googleCalendarEventId,
      'is_completed': entity.isCompleted,
      'deleted_at': entity.deletedAt?.toIso8601String(),
      'created_at': entity.createdAt.toIso8601String(),
      'updated_at': entity.updatedAt.toIso8601String(),
    };
  }

  static NoteEntity fromJson(Map<String, dynamic> json) {
    return NoteEntity(
      id: json['id'] as String,
      title: json['title'] as String,
      description: json['description'] as String,
      reminderDateTime: json['reminder_datetime'] != null
          ? DateTime.parse(json['reminder_datetime'] as String)
          : null,
      priorityLevel: _intToPriority(json['priority_level'] as int? ?? 0),
      googleCalendarEventId: json['google_calendar_event_id'] as String?,
      isCompleted: json['is_completed'] as bool? ?? false,
      isSynced: true,
      deletedAt: json['deleted_at'] != null
          ? DateTime.parse(json['deleted_at'] as String)
          : null,
      createdAt: DateTime.parse(json['created_at'] as String),
      updatedAt: DateTime.parse(json['updated_at'] as String),
    );
  }
}
