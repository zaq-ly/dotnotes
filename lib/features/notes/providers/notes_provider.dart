import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:hive_flutter/hive_flutter.dart';
import 'package:uuid/uuid.dart';
import 'package:dotnotes/features/notes/models/note.dart';
import 'package:dotnotes/core/database/boxes.dart' as boxes;

class NotesNotifier extends StateNotifier<AsyncValue<List<Note>>> {
  NotesNotifier() : super(const AsyncValue.loading()) {
    loadNotes();
  }

  Box<Note> get _box => Hive.box<Note>(boxes.notesBox);

  Future<void> loadNotes() async {
    state = const AsyncValue.loading();
    try {
      final notes = _box.values.toList();
      notes.sort((a, b) => b.updatedAt.compareTo(a.updatedAt));
      state = AsyncValue.data(notes);
    } catch (e, stack) {
      state = AsyncValue.error(e, stack);
    }
  }

  Future<void> createNote(Note note) async {
    final newNote = note.copyWith(
      id: const Uuid().v4(),
      createdAt: DateTime.now(),
      updatedAt: DateTime.now(),
    );
    await _box.put(newNote.id, newNote);
    await loadNotes();
  }

  Future<void> updateNote(Note note) async {
    final updatedNote = note.copyWith(
      updatedAt: DateTime.now(),
    );
    await _box.put(updatedNote.id, updatedNote);
    await loadNotes();
  }

  Future<void> deleteNote(String id) async {
    await _box.delete(id);
    await loadNotes();
  }

  Future<void> togglePin(String id) async {
    final note = _box.get(id);
    if (note != null) {
      final updated = note.copyWith(
        isPinned: !note.isPinned,
        updatedAt: DateTime.now(),
      );
      await _box.put(id, updated);
      await loadNotes();
    }
  }

  Future<void> toggleArchive(String id) async {
    final note = _box.get(id);
    if (note != null) {
      final updated = note.copyWith(
        isArchived: !note.isArchived,
        updatedAt: DateTime.now(),
      );
      await _box.put(id, updated);
      await loadNotes();
    }
  }

  List<Note> searchNotes(String query) {
    final notes = state.valueOrNull ?? [];
    if (query.isEmpty) return notes;
    
    final lowerQuery = query.toLowerCase();
    return notes.where((note) {
      return note.title.toLowerCase().contains(lowerQuery) ||
          note.content.toLowerCase().contains(lowerQuery);
    }).toList();
  }

  List<Note> getPinnedNotes() {
    final notes = state.valueOrNull ?? [];
    return notes.where((note) => note.isPinned && !note.isArchived).toList();
  }

  List<Note> getArchivedNotes() {
    final notes = state.valueOrNull ?? [];
    return notes.where((note) => note.isArchived).toList();
  }

  List<Note> getActiveNotes() {
    final notes = state.valueOrNull ?? [];
    return notes.where((note) => !note.isArchived).toList();
  }

  List<String> getAllCategories() {
    final notes = state.valueOrNull ?? [];
    final categories = notes
        .where((note) => note.category != null && note.category!.isNotEmpty)
        .map((note) => note.category!)
        .toSet()
        .toList();
    categories.sort();
    return categories;
  }
}

final notesProvider = StateNotifierProvider<NotesNotifier, AsyncValue<List<Note>>>((ref) {
  return NotesNotifier();
});

final pinnedNotesProvider = Provider<List<Note>>((ref) {
  final notesAsync = ref.watch(notesProvider);
  return notesAsync.valueOrNull
          ?.where((note) => note.isPinned && !note.isArchived)
          .toList() ??
      [];
});

final archivedNotesProvider = Provider<List<Note>>((ref) {
  final notesAsync = ref.watch(notesProvider);
  return notesAsync.valueOrNull?.where((note) => note.isArchived).toList() ?? [];
});

final activeNotesProvider = Provider<List<Note>>((ref) {
  final notesAsync = ref.watch(notesProvider);
  return notesAsync.valueOrNull?.where((note) => !note.isArchived).toList() ?? [];
});

final categoriesProvider = Provider<List<String>>((ref) {
  final notesAsync = ref.watch(notesProvider);
  final notes = notesAsync.valueOrNull ?? [];
  final categories = notes
      .where((note) => note.category != null && note.category!.isNotEmpty)
      .map((note) => note.category!)
      .toSet()
      .toList();
  categories.sort();
  return categories;
});
