import 'package:drift/native.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:dotnotes/data/local/drift/database.dart';
import 'package:dotnotes/data/repositories/notes_repository_impl.dart';
import 'package:dotnotes/domain/entities/note.dart';
import 'package:dotnotes/domain/usecases/create_note.dart';
import 'package:dotnotes/domain/usecases/delete_note.dart';
import 'package:dotnotes/domain/usecases/get_notes.dart';
import 'package:dotnotes/domain/usecases/update_note.dart';

void main() {
  late AppDatabase db;
  late NotesRepositoryImpl repository;
  late GetNotesUseCase getNotes;
  late CreateNoteUseCase createNote;
  late UpdateNoteUseCase updateNote;
  late DeleteNoteUseCase deleteNote;

  setUp(() {
    db = AppDatabase(NativeDatabase.memory());
    repository = NotesRepositoryImpl(db);
    getNotes = GetNotesUseCase(repository);
    createNote = CreateNoteUseCase(repository);
    updateNote = UpdateNoteUseCase(repository);
    deleteNote = DeleteNoteUseCase(repository);
  });

  tearDown(() async {
    await db.close();
  });

  test('Create, Watch, Update, and Delete flow works via UseCases', () async {
    final now = DateTime.now();
    final note = NoteEntity(
      id: 'test-1',
      title: 'First Note',
      description: 'Clean Architecture Test',
      createdAt: now,
      updatedAt: now,
    );

    await createNote(note);

    var list = await getNotes().first;
    expect(list.length, 1);
    expect(list.first.title, 'First Note');

    final updated = note.copyWith(title: 'Updated First Note');
    await updateNote(updated);

    list = await getNotes().first;
    expect(list.first.title, 'Updated First Note');

    await deleteNote('test-1');
    list = await getNotes().first;
    expect(list.isEmpty, true);
  });
}
