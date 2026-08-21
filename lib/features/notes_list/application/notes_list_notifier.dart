import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../domain/entities/note.dart';
import '../local/notes_database.dart';
import '../models/note_entity.dart';

class NotesListNotifier extends StateNotifier<List<Note>> {
  final NotesDatabase _db;

  NotesListNotifier(this._db) : super([]) {
    _load();
  }

  Future<void> _load() async {
    final entities = await _db.getAll();
    state = entities.map((e) => e.toDomain()).toList();
  }

  Future<void> add(Note note) async {
    await _db.insert(NoteEntity.fromDomain(note));
    await _load();
  }

  Future<void> update(Note note) async {
    await _db.update(NoteEntity.fromDomain(note));
    await _load();
  }

  Future<void> delete(String id) async {
    await _db.delete(id);
    await _load();
  }
}

final notesListProvider = StateNotifierProvider<NotesListNotifier, List<Note>>((ref) {
  final db = NotesDatabase.instance;
  return NotesListNotifier(db);
});

final selectedNoteIdProvider = StateProvider<String?>((ref) => null);