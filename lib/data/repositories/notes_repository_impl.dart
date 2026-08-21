import '../../domain/entities/note.dart';
import '../local/notes_database.dart';
import '../models/note_entity.dart';

class NotesRepositoryImpl {
  final NotesDatabase _db;

  NotesRepositoryImpl(this._db);

  NotesDatabase get db => _db;

  Future<List<Note>> getNotes() async {
    final entities = await _db.getAll();
    return entities.map((e) => e.toDomain()).toList();
  }

  Future<Note?> getNoteById(String id) async {
    final entity = await _db.getById(id);
    return entity?.toDomain();
  }

  Future<void> createNote(Note note) async {
    await _db.insert(NoteEntity.fromDomain(note));
  }

  Future<void> updateNote(Note note) async {
    await _db.update(NoteEntity.fromDomain(note));
  }

  Future<void> deleteNote(String id) async {
    await _db.delete(id);
  }

  Future<List<Note>> getUnsyncedNotes() async {
    final entities = await _db.getUnsynced();
    return entities.map((e) => e.toDomain()).toList();
  }

  Future<void> syncNotesToRemote(List<Note> notes) async {
    for (final note in notes) {
      await _db.update(NoteEntity.fromDomain(note));
    }
  }

  Future<List<Note>> fetchNotesFromRemote() async {
    return [];
  }
}