import 'package:drift/native.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:dotnotes/app/app.dart';
import 'package:dotnotes/data/local/drift/database.dart';
import 'package:dotnotes/features/reminder/notification_service.dart';
import 'package:dotnotes/features/settings/application/settings_notifier.dart';

class FakeNotificationService extends NotificationService {
  final List<String> scheduledNotes = [];
  final List<String> cancelledNotes = [];

  @override
  Future<void> initialize() async {}

  @override
  Future<bool> requestPermissions() async => true;

  @override
  Future<void> scheduleNoteNotification({
    required String noteId,
    required String title,
    required String body,
    required DateTime scheduledDate,
  }) async {
    scheduledNotes.add(noteId);
  }

  @override
  Future<void> cancelNoteNotification(String noteId) async {
    cancelledNotes.add(noteId);
  }
}

void main() {
  testWidgets('Reminder UI test: opens reminder modal and saves note with reminder',
      (WidgetTester tester) async {
    tester.view.physicalSize = const Size(1080, 1920);
    tester.view.devicePixelRatio = 1.0;
    addTearDown(() => tester.view.resetPhysicalSize());

    SharedPreferences.setMockInitialValues({});
    final sharedPreferences = await SharedPreferences.getInstance();
    final inMemoryDb = AppDatabase(NativeDatabase.memory());
    final fakeNotificationService = FakeNotificationService();

    await tester.pumpWidget(
      ProviderScope(
        overrides: [
          sharedPreferencesProvider.overrideWithValue(sharedPreferences),
          databaseProvider.overrideWithValue(inMemoryDb),
          notificationServiceProvider.overrideWithValue(fakeNotificationService),
        ],
        child: const DotNotesApp(),
      ),
    );
    await tester.pumpAndSettle();

    // Tap '+'
    await tester.tap(find.byIcon(Icons.add));
    await tester.pumpAndSettle();

    // Enter note title
    await tester.enterText(find.byType(TextField).first, 'Doctor Appointment');

    // Tap reminder icon
    await tester.tap(find.byIcon(Icons.notifications_none));
    await tester.pumpAndSettle();

    // Modal sheet should open
    expect(find.text('Set Reminder'), findsOneWidget);
    expect(find.text('Priority Level'), findsOneWidget);

    // Select 'Notification + Email'
    await tester.tap(find.text('Notification + Email'));
    await tester.pumpAndSettle();

    // Tap 'Done'
    await tester.tap(find.text('Done'));
    await tester.pumpAndSettle();

    // Badge should be visible in Editor
    expect(find.byIcon(Icons.notifications), findsOneWidget);

    // Save Note
    await tester.tap(find.byIcon(Icons.check));
    await tester.pumpAndSettle();

    // Verify back on Notes list
    expect(find.text('Doctor Appointment'), findsOneWidget);
    expect(find.byIcon(Icons.notifications_none), findsOneWidget);

    // Verify in DB
    final savedNotes = await (inMemoryDb.select(inMemoryDb.notesTable)).get();
    expect(savedNotes.length, 1);
    expect(savedNotes.first.priorityLevel, 1); // 1 = email
    expect(savedNotes.first.reminderDateTime != null, true);

    await inMemoryDb.close();
  });
}
