import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:uuid/uuid.dart';
import '../../../domain/entities/note.dart';
import '../../../domain/usecases/create_note.dart';
import '../../../domain/usecases/delete_note.dart';
import '../../../domain/usecases/update_note.dart';

class NoteEditorState {
  final String? id;
  final String title;
  final String description;
  final DateTime? reminderDateTime;
  final PriorityLevel priorityLevel;
  final String? googleCalendarEventId;
  final bool isCompleted;
  final bool isSaving;
  final String? error;

  const NoteEditorState({
    this.id,
    this.title = '',
    this.description = '',
    this.reminderDateTime,
    this.priorityLevel = PriorityLevel.normal,
    this.googleCalendarEventId,
    this.isCompleted = false,
    this.isSaving = false,
    this.error,
  });

  bool get isNew => id == null;

  NoteEditorState copyWith({
    String? id,
    String? title,
    String? description,
    DateTime? Function()? reminderDateTime,
    PriorityLevel? priorityLevel,
    String? Function()? googleCalendarEventId,
    bool? isCompleted,
    bool? isSaving,
    String? Function()? error,
  }) {
    return NoteEditorState(
      id: id ?? this.id,
      title: title ?? this.title,
      description: description ?? this.description,
      reminderDateTime: reminderDateTime != null
          ? reminderDateTime()
          : this.reminderDateTime,
      priorityLevel: priorityLevel ?? this.priorityLevel,
      googleCalendarEventId: googleCalendarEventId != null
          ? googleCalendarEventId()
          : this.googleCalendarEventId,
      isCompleted: isCompleted ?? this.isCompleted,
      isSaving: isSaving ?? this.isSaving,
      error: error != null ? error() : this.error,
    );
  }
}

class NoteEditorNotifier extends StateNotifier<NoteEditorState> {
  final CreateNoteUseCase _createNote;
  final UpdateNoteUseCase _updateNote;
  final DeleteNoteUseCase _deleteNote;

  NoteEditorNotifier({
    required CreateNoteUseCase createNoteUseCase,
    required UpdateNoteUseCase updateNoteUseCase,
    required DeleteNoteUseCase deleteNoteUseCase,
    NoteEntity? initialNote,
  })  : _createNote = createNoteUseCase,
        _updateNote = updateNoteUseCase,
        _deleteNote = deleteNoteUseCase,
        super(
          initialNote != null
              ? NoteEditorState(
                  id: initialNote.id,
                  title: initialNote.title,
                  description: initialNote.description,
                  reminderDateTime: initialNote.reminderDateTime,
                  priorityLevel: initialNote.priorityLevel,
                  googleCalendarEventId: initialNote.googleCalendarEventId,
                  isCompleted: initialNote.isCompleted,
                )
              : const NoteEditorState(),
        );

  void updateTitle(String title) {
    state = state.copyWith(title: title);
  }

  void updateDescription(String description) {
    state = state.copyWith(description: description);
  }

  void updateReminder(DateTime? dateTime, PriorityLevel priority) {
    state = state.copyWith(
      reminderDateTime: () => dateTime,
      priorityLevel: priority,
    );
  }

  Future<bool> saveNote() async {
    final title = state.title.trim();
    final description = state.description.trim();

    if (title.isEmpty && description.isEmpty) {
      return false;
    }

    state = state.copyWith(isSaving: true, error: () => null);

    try {
      final now = DateTime.now();
      if (state.isNew) {
        final newId = const Uuid().v4();
        final newNote = NoteEntity(
          id: newId,
          title: title.isEmpty ? 'Untitled' : title,
          description: description,
          reminderDateTime: state.reminderDateTime,
          priorityLevel: state.priorityLevel,
          googleCalendarEventId: state.googleCalendarEventId,
          isCompleted: state.isCompleted,
          isSynced: false,
          createdAt: now,
          updatedAt: now,
        );
        await _createNote(newNote);
        state = state.copyWith(id: newId, isSaving: false);
      } else {
        final updatedNote = NoteEntity(
          id: state.id!,
          title: title.isEmpty ? 'Untitled' : title,
          description: description,
          reminderDateTime: state.reminderDateTime,
          priorityLevel: state.priorityLevel,
          googleCalendarEventId: state.googleCalendarEventId,
          isCompleted: state.isCompleted,
          isSynced: false,
          createdAt: now,
          updatedAt: now,
        );
        await _updateNote(updatedNote);
        state = state.copyWith(isSaving: false);
      }
      return true;
    } catch (e) {
      state = state.copyWith(isSaving: false, error: () => e.toString());
      return false;
    }
  }

  Future<bool> deleteCurrentNote() async {
    if (state.id == null) return false;
    state = state.copyWith(isSaving: true);
    try {
      await _deleteNote(state.id!);
      state = state.copyWith(isSaving: false);
      return true;
    } catch (e) {
      state = state.copyWith(isSaving: false, error: () => e.toString());
      return false;
    }
  }
}

final noteEditorNotifierProvider = StateNotifierProvider.autoDispose
    .family<NoteEditorNotifier, NoteEditorState, NoteEntity?>((ref, note) {
  final createNote = ref.watch(createNoteUseCaseProvider);
  final updateNote = ref.watch(updateNoteUseCaseProvider);
  final deleteNote = ref.watch(deleteNoteUseCaseProvider);

  return NoteEditorNotifier(
    createNoteUseCase: createNote,
    updateNoteUseCase: updateNote,
    deleteNoteUseCase: deleteNote,
    initialNote: note,
  );
});
