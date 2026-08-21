enum PriorityLevel { normal, email, alarm }

class Note {
  final String id;
  final String? userId;
  final String title;
  final String description;
  final DateTime? reminderDatetime;
  final PriorityLevel priorityLevel;
  final bool isCompleted;
  final DateTime createdAt;
  final DateTime updatedAt;
  final bool isSynced;
  final bool isDeleted;

  const Note({
    required this.id,
    this.userId,
    required this.title,
    required this.description,
    this.reminderDatetime,
    required this.priorityLevel,
    this.isCompleted = false,
    required this.createdAt,
    required this.updatedAt,
    this.isSynced = false,
    this.isDeleted = false,
  });

  Note copyWith({
    String? id,
    String? userId,
    String? title,
    String? description,
    DateTime? reminderDatetime,
    PriorityLevel? priorityLevel,
    bool? isCompleted,
    DateTime? createdAt,
    DateTime? updatedAt,
    bool? isSynced,
    bool? isDeleted,
  }) {
    return Note(
      id: id ?? this.id,
      userId: userId ?? this.userId,
      title: title ?? this.title,
      description: description ?? this.description,
      reminderDatetime: reminderDatetime ?? this.reminderDatetime,
      priorityLevel: priorityLevel ?? this.priorityLevel,
      isCompleted: isCompleted ?? this.isCompleted,
      createdAt: createdAt ?? this.createdAt,
      updatedAt: updatedAt ?? this.updatedAt,
      isSynced: isSynced ?? this.isSynced,
      isDeleted: isDeleted ?? this.isDeleted,
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'id': id,
      'user_id': userId,
      'title': title,
      'description': description,
      'reminder_datetime': reminderDatetime?.toIso8601String(),
      'priority_level': priorityLevel.name,
      'is_completed': isCompleted,
      'created_at': createdAt.toIso8601String(),
      'updated_at': updatedAt.toIso8601String(),
      'is_synced': isSynced,
      'is_deleted': isDeleted,
    };
  }
}