import '../entities/note.dart';

abstract class NotesRepository {
  Stream<List<NoteEntity>> watchNotes();
  Future<NoteEntity?> getNoteById(String id);
  Future<void> createNote(NoteEntity note);
  Future<void> updateNote(NoteEntity note);
  Future<void> deleteNote(String id);
  Future<void> syncWithGoogleDrive();
}
