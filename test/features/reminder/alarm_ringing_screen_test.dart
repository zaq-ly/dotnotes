import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:dotnotes/features/reminder/alarm_service.dart';
import 'package:dotnotes/features/reminder/notification_service.dart';
import 'package:dotnotes/features/reminder/presentation/alarm_ringing_screen.dart';
import 'package:dotnotes/l10n/app_localizations.dart';

class FakeAlarmService extends AlarmService {
  final List<String> scheduledAlarms = [];

  @override
  Future<bool> scheduleAlarm({
    required String noteId,
    required DateTime scheduledDate,
    required String title,
    required String description,
  }) async {
    scheduledAlarms.add(noteId);
    return true;
  }
}

class FakeNotificationService extends NotificationService {
  final List<String> scheduledNotes = [];

  @override
  Future<void> scheduleNoteNotification({
    required String noteId,
    required String title,
    required String body,
    required DateTime scheduledDate,
  }) async {
    scheduledNotes.add(noteId);
  }
}

void main() {
  testWidgets('AlarmRingingScreen renders content and handles Snooze/Dismiss',
      (WidgetTester tester) async {
    final fakeAlarmService = FakeAlarmService();
    final fakeNotifService = FakeNotificationService();

    await tester.pumpWidget(
      ProviderScope(
        overrides: [
          alarmServiceProvider.overrideWithValue(fakeAlarmService),
          notificationServiceProvider.overrideWithValue(fakeNotifService),
        ],
        child: const MaterialApp(
          localizationsDelegates: AppLocalizations.localizationsDelegates,
          supportedLocales: AppLocalizations.supportedLocales,
          home: AlarmRingingScreen(
            arguments: AlarmArguments(
              noteId: 'urgent-note-1',
              title: 'Pay Electricity Bill',
              description: 'Due by midnight today!',
            ),
          ),
        ),
      ),
    );
    await tester.pump();

    // Verify content rendered
    expect(find.text('Pay Electricity Bill'), findsOneWidget);
    expect(find.text('Due by midnight today!'), findsOneWidget);
    expect(find.byIcon(Icons.alarm_on), findsOneWidget);
    expect(find.text('Dismiss'), findsOneWidget);
    expect(find.textContaining('Snooze'), findsOneWidget);

    // Test Snooze click
    await tester.tap(find.textContaining('Snooze'));
    await tester.pump();

    expect(fakeAlarmService.scheduledAlarms.contains('urgent-note-1'), true);
    expect(fakeNotifService.scheduledNotes.contains('urgent-note-1'), true);
  });
}
