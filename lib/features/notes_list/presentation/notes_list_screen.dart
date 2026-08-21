import 'package:flutter/material.dart';
import '../../../core/extensions/extensions.dart';
import '../../../data/local/notes_database.dart';
import '../../../data/models/note_entity.dart';
import '../../../domain/entities/note.dart';

class NotesListScreen extends StatelessWidget {
  const NotesListScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('.notes'),
        actions: [
          IconButton(
            icon: const Icon(Icons.settings_outlined),
            onPressed: () => Navigator.of(context).pushNamed('/settings'),
          ),
        ],
      ),
      body: FutureBuilder<List<NoteEntity>>(
        future: NotesDatabase.instance.getAll(),
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          }
          final notes = snapshot.data ?? [];
          if (notes.isEmpty) {
            return const Center(child: Text('No notes yet'));
          }
          return ListView.builder(
            padding: const EdgeInsets.all(16),
            itemCount: notes.length,
            itemBuilder: (context, index) {
              final note = notes[index].toDomain();
              return _NoteCard(note: note);
            },
          );
        },
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () => Navigator.of(context).pushNamed('/notes/editor'),
        child: const Icon(Icons.add),
      ),
    );
  }
}

class _NoteCard extends StatelessWidget {
  final Note note;
  const _NoteCard({required this.note});

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
    return Card(
      margin: const EdgeInsets.only(bottom: 8),
      child: Row(
        children: [
          Container(
            width: 3,
            height: 60,
            decoration: BoxDecoration(
              color: _indicatorColor(),
              borderRadius: const BorderRadius.only(
                topLeft: Radius.circular(12),
                bottomLeft: Radius.circular(12),
              ),
            ),
          ),
          Expanded(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(note.title, style: context.textTheme.titleMedium),
                  const SizedBox(height: 4),
                  Text(note.description, style: context.textTheme.bodySmall, maxLines: 1, overflow: TextOverflow.ellipsis),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}