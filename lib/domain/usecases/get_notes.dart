import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../data/repositories/notes_repository_impl.dart';
import '../entities/note.dart';
import '../repositories/notes_repository.dart';

class GetNotesUseCase {
  final NotesRepository _repository;

  GetNotesUseCase(this._repository);

  Stream<List<NoteEntity>> call() {
    return _repository.watchNotes();
  }
}

final getNotesUseCaseProvider = Provider<GetNotesUseCase>((ref) {
  final repository = ref.watch(notesRepositoryProvider);
  return GetNotesUseCase(repository);
});
