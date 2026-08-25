import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../domain/entities/note.dart';
import '../../domain/repositories/notes_repository.dart';
import '../local/drift/database.dart';
import '../models/note_model.dart';

class NotesRepositoryImpl implements NotesRepository {
  final AppDatabase _db;

  NotesRepositoryImpl(this._db);

  @override
  Stream<List<NoteEntity>> watchNotes() {
    return _db.watchActiveNotes().map(
          (rows) => rows.map(NoteModel.fromDrift).toList(),
        );
  }

  @override
  Future<NoteEntity?> getNoteById(String id) async {
    final row = await _db.getNoteById(id);
    return row != null ? NoteModel.fromDrift(row) : null;
  }

  @override
  Future<void> createNote(NoteEntity note) async {
    await _db.insertNote(NoteModel.toCompanion(note));
  }

  @override
  Future<void> updateNote(NoteEntity note) async {
    await _db.updateNote(NoteModel.toCompanion(note));
  }

  @override
  Future<void> deleteNote(String id) async {
    await _db.softDeleteNote(id, DateTime.now());
  }

  @override
  Future<void> syncWithGoogleDrive() async {
    // TODO: Google Drive Sync Implementation in Phase 1B/2
  }
}

final notesRepositoryProvider = Provider<NotesRepository>((ref) {
  final db = ref.watch(databaseProvider);
  return NotesRepositoryImpl(db);
});
