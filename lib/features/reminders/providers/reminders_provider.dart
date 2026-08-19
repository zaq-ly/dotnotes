import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:hive_flutter/hive_flutter.dart';
import 'package:uuid/uuid.dart';
import 'package:dotnotes/features/reminders/models/reminder.dart';
import 'package:dotnotes/core/database/boxes.dart' as boxes;
import 'package:dotnotes/core/services/notification_service.dart';

class RemindersNotifier extends StateNotifier<AsyncValue<List<Reminder>>> {
  RemindersNotifier() : super(const AsyncValue.loading()) {
    loadReminders();
  }

  Box<Reminder> get _box => Hive.box<Reminder>(boxes.remindersBox);

  Future<void> loadReminders() async {
    state = const AsyncValue.loading();
    try {
      final reminders = _box.values.toList();
      reminders.sort((a, b) => a.scheduledAt.compareTo(b.scheduledAt));
      state = AsyncValue.data(reminders);
    } catch (e, stack) {
      state = AsyncValue.error(e, stack);
    }
  }

  Future<void> createReminder(Reminder reminder) async {
    final newReminder = reminder.copyWith(
      id: const Uuid().v4(),
    );
    await _box.put(newReminder.id, newReminder);

    if (newReminder.alarmEnabled) {
      await _scheduleNotification(newReminder);
    }

    await loadReminders();
  }

  Future<void> updateReminder(Reminder reminder) async {
    await _box.put(reminder.id, reminder);

    await NotificationService().cancelNotification(reminder.id.hashCode);

    if (reminder.alarmEnabled && reminder.status != ReminderStatus.completed) {
      await _scheduleNotification(reminder);
    }

    await loadReminders();
  }

  Future<void> deleteReminder(String id) async {
    await NotificationService().cancelNotification(id.hashCode);
    await _box.delete(id);
    await loadReminders();
  }

  Future<void> completeReminder(String id) async {
    final reminder = _box.get(id);
    if (reminder != null) {
      final updated = reminder.copyWith(status: ReminderStatus.completed);
      await updateReminder(updated);
    }
  }

  Future<void> snoozeReminder(String id, Duration duration) async {
    final reminder = _box.get(id);
    if (reminder != null) {
      final newTime = DateTime.now().add(duration);
      final updated = reminder.copyWith(
        scheduledAt: newTime,
        status: ReminderStatus.snoozed,
      );
      await updateReminder(updated);
    }
  }

  Future<void> _scheduleNotification(Reminder reminder) async {
    await NotificationService().scheduleNotification(
      id: reminder.id.hashCode,
      title: 'Reminder',
      body: reminder.noteId,
      scheduledTime: reminder.scheduledAt,
    );
  }
}

final remindersProvider =
    StateNotifierProvider<RemindersNotifier, AsyncValue<List<Reminder>>>((ref) {
  return RemindersNotifier();
});

final todayRemindersProvider = Provider<List<Reminder>>((ref) {
  final remindersAsync = ref.watch(remindersProvider);
  final reminders = remindersAsync.valueOrNull ?? [];
  final now = DateTime.now();
  final today = DateTime(now.year, now.month, now.day);
  final tomorrow = today.add(const Duration(days: 1));

  return reminders
      .where(
        (r) =>
            r.scheduledAt.isAfter(today) &&
            r.scheduledAt.isBefore(tomorrow) &&
            r.status != ReminderStatus.completed,
      )
      .toList();
});

final upcomingRemindersProvider = Provider<List<Reminder>>((ref) {
  final remindersAsync = ref.watch(remindersProvider);
  final reminders = remindersAsync.valueOrNull ?? [];
  final now = DateTime.now();
  final tomorrow = DateTime(now.year, now.month, now.day).add(const Duration(days: 1));

  return reminders
      .where(
        (r) =>
            r.scheduledAt.isAfter(tomorrow) &&
            r.status != ReminderStatus.completed,
      )
      .take(5)
      .toList();
});
