import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../domain/entities/note.dart';
import '../../domain/usecases/get_notes.dart';
import 'alarm_service.dart';
import 'notification_service.dart';

class ReminderSyncService {
  final GetNotesUseCase _getNotes;
  final NotificationService _notificationService;
  final AlarmService _alarmService;

  ReminderSyncService({
    required GetNotesUseCase getNotesUseCase,
    required NotificationService notificationServiceUseCase,
    required AlarmService alarmServiceUseCase,
  })  : _getNotes = getNotesUseCase,
        _notificationService = notificationServiceUseCase,
        _alarmService = alarmServiceUseCase;

  Future<void> rescheduleAllActiveReminders() async {
    final notes = await _getNotes().first;
    final now = DateTime.now();

    for (final note in notes) {
      if (note.reminderDateTime != null &&
          note.reminderDateTime!.isAfter(now) &&
          !note.isCompleted) {
        // 1. Reschedule Local Notification
        await _notificationService.scheduleNoteNotification(
          noteId: note.id,
          title: note.title,
          body: note.description,
          scheduledDate: note.reminderDateTime!,
        );

        // 2. Reschedule Alarm Level 3
        if (note.priorityLevel == PriorityLevel.alarm) {
          await _alarmService.scheduleAlarm(
            noteId: note.id,
            scheduledDate: note.reminderDateTime!,
            title: note.title,
            description: note.description,
          );
        }
      }
    }
  }
}

final reminderSyncServiceProvider = Provider<ReminderSyncService>((ref) {
  final getNotes = ref.watch(getNotesUseCaseProvider);
  final notificationService = ref.watch(notificationServiceProvider);
  final alarmService = ref.watch(alarmServiceProvider);
  return ReminderSyncService(
    getNotesUseCase: getNotes,
    notificationServiceUseCase: notificationService,
    alarmServiceUseCase: alarmService,
  );
});
