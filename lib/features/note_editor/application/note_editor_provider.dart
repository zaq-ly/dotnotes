import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../data/local/notes_database.dart';
import '../../../data/models/note_entity.dart';

class NoteEditorNotifier extends StateNotifier<NoteEntity?> {
  final NotesDatabase _db;

  NoteEditorNotifier(this._db) : super(null);

  void createNew() {
    state = NoteEntity(
      id: DateTime.now().millisecondsSinceEpoch.toString(),
      title: '',
      description: '',
      priorityLevel: 1,
      isCompleted: false,
      createdAt: DateTime.now().millisecondsSinceEpoch,
      updatedAt: DateTime.now().millisecondsSinceEpoch,
      isSynced: false,
      isDeleted: false,
    );
  }

  void setNote(NoteEntity note) {
    state = note;
  }

  void setTitle(String title) {
    if (state == null) return;
    state = state!.copyWith(title: title);
  }

  void setDescription(String description) {
    if (state == null) return;
    state = state!.copyWith(description: description);
  }

  void setReminder(int? milliseconds) {
    if (state == null) return;
    state = state!.copyWith(reminderDatetime: milliseconds);
  }

  void setPriority(int priority) {
    if (state == null) return;
    state = state!.copyWith(priorityLevel: priority);
  }

  Future<void> save() async {
    if (state == null) return;
    await _db.insert(state!);
    state = null;
  }

  Future<void> update() async {
    if (state == null) return;
    await _db.update(state!);
    state = null;
  }
}

final noteEditorProvider = StateNotifierProvider<NoteEditorNotifier, NoteEntity?>((ref) {
  final db = NotesDatabase.instance;
  return NoteEditorNotifier(db);
});

final noteIdProvider = StateProvider<String?>((ref) => null);