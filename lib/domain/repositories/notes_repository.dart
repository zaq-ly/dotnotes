import '../entities/note.dart';

abstract class NotesRepository {
  Future<Note> createNote(Note note);
  Future<Note> updateNote(Note note);
  Future<void> deleteNote(String id);
  Future<List<Note>> getNotes();
  Future<Note?> getNoteById(String id);
  Future<List<Note>> getUnsyncedNotes();
  Future<void> syncNotesToRemote(List<Note> notes);
  Future<List<Note>> fetchNotesFromRemote();
}