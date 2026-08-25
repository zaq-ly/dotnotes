import 'package:drift/native.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:dotnotes/app/app.dart';
import 'package:dotnotes/data/local/drift/database.dart';
import 'package:dotnotes/features/settings/application/settings_notifier.dart';

void main() {
  testWidgets('Notes full UI test: can create note and view in list',
      (WidgetTester tester) async {
    SharedPreferences.setMockInitialValues({});
    final sharedPreferences = await SharedPreferences.getInstance();
    final inMemoryDb = AppDatabase(NativeDatabase.memory());

    await tester.pumpWidget(
      ProviderScope(
        overrides: [
          sharedPreferencesProvider.overrideWithValue(sharedPreferences),
          databaseProvider.overrideWithValue(inMemoryDb),
        ],
        child: const DotNotesApp(),
      ),
    );
    await tester.pumpAndSettle();

    expect(find.text('.notes'), findsOneWidget);

    // Tap '+' to open NoteEditorScreen
    await tester.tap(find.byIcon(Icons.add));
    await tester.pumpAndSettle();

    expect(find.byType(TextField), findsNWidgets(2));

    // Enter title & description
    await tester.enterText(find.byType(TextField).first, 'Groceries');
    await tester.enterText(find.byType(TextField).last, 'Apples, Milk');

    // Tap Save
    await tester.tap(find.byIcon(Icons.check));
    await tester.pumpAndSettle();

    // Verify back on NotesListScreen and note card is rendered
    expect(find.text('Groceries'), findsOneWidget);
    expect(find.text('Apples, Milk'), findsOneWidget);

    await inMemoryDb.close();
  });
}
