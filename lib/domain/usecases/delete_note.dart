import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../data/repositories/notes_repository_impl.dart';
import '../repositories/notes_repository.dart';

class DeleteNoteUseCase {
  final NotesRepository _repository;

  DeleteNoteUseCase(this._repository);

  Future<void> call(String id) {
    return _repository.deleteNote(id);
  }
}

final deleteNoteUseCaseProvider = Provider<DeleteNoteUseCase>((ref) {
  final repository = ref.watch(notesRepositoryProvider);
  return DeleteNoteUseCase(repository);
});
