import 'package:drift/native.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:dotnotes/data/local/drift/database.dart';

void main() {
  late AppDatabase db;

  setUp(() {
    db = AppDatabase(NativeDatabase.memory());
  });

  tearDown(() async {
    await db.close();
  });

  test('Database inserts and queries active notes correctly', () async {
    final now = DateTime.now();
    await db.insertNote(
      NotesTableCompanion.insert(
        id: '1',
        title: 'Meeting Notes',
        description: 'Discuss Q3 goals',
        createdAt: now,
        updatedAt: now,
      ),
    );

    final notes = await db.watchActiveNotes().first;
    expect(notes.length, 1);
    expect(notes.first.title, 'Meeting Notes');
    expect(notes.first.description, 'Discuss Q3 goals');
  });

  test('Database soft-deletes notes and excludes from active query', () async {
    final now = DateTime.now();
    await db.insertNote(
      NotesTableCompanion.insert(
        id: '2',
        title: 'Shopping List',
        description: 'Milk, Eggs, Bread',
        createdAt: now,
        updatedAt: now,
      ),
    );

    var notes = await db.watchActiveNotes().first;
    expect(notes.length, 1);

    await db.softDeleteNote('2', DateTime.now());

    notes = await db.watchActiveNotes().first;
    expect(notes.isEmpty, true);
  });
}
