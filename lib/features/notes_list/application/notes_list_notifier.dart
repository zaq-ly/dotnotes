import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../domain/entities/note.dart';
import '../../../data/local/notes_database.dart';
import '../../../data/models/note_entity.dart';

class NotesRepositoryImpl {
  final NotesDatabase db;

  NotesRepositoryImpl(this.db);

  Future<List<Note>> getNotes() async {
    final entities = await db.getAll();
    return entities.map((e) => e.toDomain()).toList();
  }

  Future<Note?> getNoteById(String id) async {
    final entity = await db.getById(id);
    return entity?.toDomain();
  }

  Future<void> createNote(Note note) async {
    await db.insert(NoteEntity.fromDomain(note));
  }

  Future<void> updateNote(Note note) async {
    await db.update(NoteEntity.fromDomain(note));
  }

  Future<void> deleteNote(String id) async {
    await db.delete(id);
  }

  Future<List<Note>> getUnsyncedNotes() async {
    final entities = await db.getUnsynced();
    return entities.map((e) => e.toDomain()).toList();
  }

  Future<void> syncNotesToRemote(List<Note> notes) async {
    for (final note in notes) {
      await db.update(NoteEntity.fromDomain(note));
    }
  }

  Future<List<Note>> fetchNotesFromRemote() async {
    return [];
  }
}

class NotesListNotifier extends StateNotifier<List<Note>> {
  final NotesRepositoryImpl _repo;

  NotesListNotifier(this._repo) : super([]) {
    _load();
  }

  Future<void> _load() async {
    state = await _repo.getNotes();
  }

  Future<void> add(Note note) async {
    await _repo.createNote(note);
    await _load();
  }

  Future<void> update(Note note) async {
    await _repo.updateNote(note);
    await _load();
  }

  Future<void> delete(String id) async {
    await _repo.deleteNote(id);
    await _load();
  }
}

final notesListProvider = StateNotifierProvider<NotesListNotifier, List<Note>>((ref) {
  final db = NotesDatabase.instance;
  return NotesListNotifier(NotesRepositoryImpl(db));
});

final selectedNoteIdProvider = StateProvider<String?>((ref) => null);