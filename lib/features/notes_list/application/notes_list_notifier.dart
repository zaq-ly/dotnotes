import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../domain/entities/note.dart';
import '../../../domain/usecases/get_notes.dart';

final searchQueryProvider = StateProvider<String>((ref) => '');

final notesStreamProvider = StreamProvider<List<NoteEntity>>((ref) {
  final getNotes = ref.watch(getNotesUseCaseProvider);
  final query = ref.watch(searchQueryProvider).toLowerCase().trim();

  return getNotes().map((notes) {
    if (query.isEmpty) return notes;
    return notes.where((note) {
      final titleMatch = note.title.toLowerCase().contains(query);
      final descMatch = note.description.toLowerCase().contains(query);
      return titleMatch || descMatch;
    }).toList();
  });
});
