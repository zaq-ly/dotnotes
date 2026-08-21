import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/extensions/extensions.dart';
import '../../../domain/entities/note.dart';
import '../../../data/local/notes_database.dart';
import '../../../data/models/note_entity.dart';
import 'notes_list_notifier.dart';
import 'note_editor_provider.dart';

class NotesListScreen extends ConsumerWidget {
  const NotesListScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final notes = ref.watch(notesListProvider);
    final editNote = ref.read(noteEditorProvider.notifier);

    return Scaffold(
      appBar: AppBar(title: const Text('.notes'), actions: [IconButton(icon: const Icon(Icons.settings_outlined), onPressed: () => Navigator.of(context).pushNamed('/settings'))]),
      body: notes.isEmpty ? const Center(child: Text('No notes yet')) : ListView.builder(padding: const EdgeInsets.all(16), itemCount: notes.length, itemBuilder: (context, index) {
        final note = notes[index];
        return Dismissible(key: Key(note.id), background: Container(color: Colors.red, alignment: Alignment.centerLeft, padding: const EdgeInsets.only(left: 16), child: const Icon(Icons.delete, color: Colors.white)), secondaryBackground: Container(color: Colors.red, alignment: Alignment.centerRight, padding: const EdgeInsets.only(right: 16), child: const Icon(Icons.delete, color: Colors.white)), direction: DismissDirection.startToEnd, onDismissed: (_) async { await ref.read(notesListProvider.notifier).delete(note.id); }, child: _NoteCard(note: note, onTap: () => _editNote(context, ref, note, editNote)));
      }),
      floatingActionButton: FloatingActionButton(onPressed: () => _createNote(context, ref, editNote), child: const Icon(Icons.add)),
    );
  }

  void _createNote(BuildContext context, WidgetRef ref, NoteEditorNotifier editNote) {
    editNote.createNew();
    Navigator.of(context).pushNamed('/notes/editor');
  }

  void _editNote(BuildContext context, WidgetRef ref, Note note, NoteEditorNotifier editNote) {
    editNote.setNote(NoteEntity.fromDomain(note));
    Navigator.of(context).pushNamed('/notes/editor');
  }
}

class _NoteCard extends StatelessWidget {
  final Note note;
  final VoidCallback onTap;
  const _NoteCard({required this.note, required this.onTap});

  Color _indicatorColor() {
    switch (note.priorityLevel) {
      case PriorityLevel.normal:
        return const Color(0xFF9CA3AF);
      case PriorityLevel.email:
        return const Color(0xFFF5B94C);
      case PriorityLevel.alarm:
        return const Color(0xFFEF6B6B);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Card(margin: const EdgeInsets.only(bottom: 8), child: InkWell(onTap: onTap, child: Row(children: [Container(width: 3, height: 60, decoration: BoxDecoration(color: _indicatorColor(), borderRadius: const BorderRadius.only(topLeft: Radius.circular(12), bottomLeft: Radius.circular(12)))), Expanded(child: Padding(padding: const EdgeInsets.all(16), child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [Text(note.title, style: context.textTheme.titleMedium), const SizedBox(height: 4), Text(note.description, style: context.textTheme.bodySmall, maxLines: 1, overflow: TextOverflow.ellipsis)])))]));
  }
}