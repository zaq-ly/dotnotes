import '../../domain/entities/note.dart';
import '../../domain/repositories/notes_repository.dart';
import '../local/notes_database.dart';
import '../models/note_entity.dart';

class NotesRepositoryImpl implements NotesRepository {
  final NotesDatabase _db;

  NotesRepositoryImpl(this._db);

  @override
  Future<Note> createNote(Note note) async {
    final entity = NoteEntity.fromDomain(note);
    await _db.insert(entity);
    return entity.toDomain();
  }

  @override
  Future<Note> updateNote(Note note) async {
    final entity = NoteEntity.fromDomain(note);
    await _db.update(entity);
    return entity.toDomain();
  }

  @override
  Future<void> deleteNote(String id) async {
    await _db.delete(id);
  }

  @override
  Future<List<Note>> getNotes() async {
    final entities = await _db.getAll();
    return entities.map((e) => e.toDomain()).toList();
  }

  @override
  Future<Note?> getNoteById(String id) async {
    final entity = await _db.getById(id);
    return entity?.toDomain();
  }

  @override
  Future<List<Note>> getUnsyncedNotes() async {
    final entities = await _db.getUnsynced();
    return entities.map((e) => e.toDomain()).toList();
  }

  @override
  Future<void> syncNotesToRemote(List<Note> notes) async {
    for (final note in notes) {
      await _db.update(NoteEntity.fromDomain(note));
    }
  }

  @override
  Future<List<Note>> fetchNotesFromRemote() {
    return Future.value([]);
  }
}