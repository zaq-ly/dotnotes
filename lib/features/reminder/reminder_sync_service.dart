import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../domain/usecases/get_notes.dart';
import 'notification_service.dart';

class ReminderSyncService {
  final GetNotesUseCase _getNotes;
  final NotificationService _notificationService;

  ReminderSyncService({
    required GetNotesUseCase getNotesUseCase,
    required NotificationService notificationServiceUseCase,
  })  : _getNotes = getNotesUseCase,
        _notificationService = notificationServiceUseCase;

  Future<void> rescheduleAllActiveReminders() async {
    final notes = await _getNotes().first;
    final now = DateTime.now();

    for (final note in notes) {
      if (note.reminderDateTime != null &&
          note.reminderDateTime!.isAfter(now) &&
          !note.isCompleted) {
        await _notificationService.scheduleNoteNotification(
          noteId: note.id,
          title: note.title,
          body: note.description,
          scheduledDate: note.reminderDateTime!,
        );
      }
    }
  }
}

final reminderSyncServiceProvider = Provider<ReminderSyncService>((ref) {
  final getNotes = ref.watch(getNotesUseCaseProvider);
  final notificationService = ref.watch(notificationServiceProvider);
  return ReminderSyncService(
    getNotesUseCase: getNotes,
    notificationServiceUseCase: notificationService,
  );
});
