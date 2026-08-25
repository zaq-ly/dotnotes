import 'dart:io';
import 'package:drift/drift.dart';
import 'package:drift/native.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:path_provider/path_provider.dart';
import 'package:path/path.dart' as p;
import 'tables/notes_table.dart';

part 'database.g.dart';

@DriftDatabase(tables: [NotesTable])
class AppDatabase extends _$AppDatabase {
  AppDatabase([QueryExecutor? e]) : super(e ?? _openConnection());

  @override
  int get schemaVersion => 1;

  static LazyDatabase _openConnection() {
    return LazyDatabase(() async {
      final dbFolder = await getApplicationDocumentsDirectory();
      final file = File(p.join(dbFolder.path, 'dotnotes.sqlite'));
      return NativeDatabase.createInBackground(file);
    });
  }

  // Queries
  Stream<List<NotesTableData>> watchActiveNotes() {
    return (select(notesTable)
          ..where((t) => t.deletedAt.isNull())
          ..orderBy([
            (t) => OrderingTerm(expression: t.updatedAt, mode: OrderingMode.desc),
          ]))
        .watch();
  }

  Future<NotesTableData?> getNoteById(String id) {
    return (select(notesTable)..where((t) => t.id.equals(id)))
        .getSingleOrNull();
  }

  Future<int> insertNote(NotesTableCompanion entry) {
    return into(notesTable).insert(entry, mode: InsertMode.insertOrReplace);
  }

  Future<bool> updateNote(NotesTableCompanion entry) {
    return update(notesTable).replace(entry);
  }

  Future<int> softDeleteNote(String id, DateTime deletedAt) {
    return (update(notesTable)..where((t) => t.id.equals(id))).write(
      NotesTableCompanion(
        deletedAt: Value(deletedAt),
        isSynced: const Value(false),
        updatedAt: Value(deletedAt),
      ),
    );
  }

  Future<int> hardDeleteNote(String id) {
    return (delete(notesTable)..where((t) => t.id.equals(id))).go();
  }

  Future<List<NotesTableData>> getUnsyncedNotes() {
    return (select(notesTable)..where((t) => t.isSynced.equals(false))).get();
  }

  Future<int> markAsSynced(String id) {
    return (update(notesTable)..where((t) => t.id.equals(id))).write(
      const NotesTableCompanion(
        isSynced: Value(true),
      ),
    );
  }
}

final databaseProvider = Provider<AppDatabase>((ref) {
  final db = AppDatabase();
  ref.onDispose(db.close);
  return db;
});
