import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../data/repositories/notes_repository_impl.dart';
import '../entities/note.dart';
import '../repositories/notes_repository.dart';

class CreateNoteUseCase {
  final NotesRepository _repository;

  CreateNoteUseCase(this._repository);

  Future<void> call(NoteEntity note) {
    return _repository.createNote(note);
  }
}

final createNoteUseCaseProvider = Provider<CreateNoteUseCase>((ref) {
  final repository = ref.watch(notesRepositoryProvider);
  return CreateNoteUseCase(repository);
});
