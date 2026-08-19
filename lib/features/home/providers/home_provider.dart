import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:dotnotes/features/notes/models/note.dart';
import 'package:dotnotes/features/notes/providers/notes_provider.dart';
import 'package:dotnotes/features/reminders/models/reminder.dart';
import 'package:dotnotes/features/reminders/providers/reminders_provider.dart';

class HomeData {
  final List<Note> recentNotes;
  final List<Reminder> todayReminders;
  final List<Reminder> upcomingReminders;

  HomeData({
    required this.recentNotes,
    required this.todayReminders,
    required this.upcomingReminders,
  });
}

final homeDataProvider = Provider<AsyncValue<HomeData>>((ref) {
  final notesAsync = ref.watch(notesProvider);
  final todayReminders = ref.watch(todayRemindersProvider);
  final upcomingReminders = ref.watch(upcomingRemindersProvider);

  return notesAsync.when(
    data: (notes) {
      final activeNotes = notes.where((note) => !note.isArchived).toList();
      activeNotes.sort((a, b) => b.updatedAt.compareTo(a.updatedAt));
      final recentNotes = activeNotes.take(5).toList();

      return AsyncValue.data(
        HomeData(
          recentNotes: recentNotes,
          todayReminders: todayReminders,
          upcomingReminders: upcomingReminders,
        ),
      );
    },
    loading: () => const AsyncValue.loading(),
    error: (err, stack) => AsyncValue.error(err, stack),
  );
});
