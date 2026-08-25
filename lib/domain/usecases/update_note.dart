import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../data/repositories/notes_repository_impl.dart';
import '../entities/note.dart';
import '../repositories/notes_repository.dart';

class UpdateNoteUseCase {
  final NotesRepository _repository;

  UpdateNoteUseCase(this._repository);

  Future<void> call(NoteEntity note) {
    return _repository.updateNote(note);
  }
}

final updateNoteUseCaseProvider = Provider<UpdateNoteUseCase>((ref) {
  final repository = ref.watch(notesRepositoryProvider);
  return UpdateNoteUseCase(repository);
});
