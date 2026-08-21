import 'package:flutter/material.dart';
import '../../../core/extensions/extensions.dart';
import '../../../data/local/notes_database.dart';
import '../../../data/models/note_entity.dart';
import '../../../domain/entities/note.dart';

class NoteEditorScreen extends StatefulWidget {
  const NoteEditorScreen({super.key});

  @override
  State<NoteEditorScreen> createState() => _NoteEditorScreenState();
}

class _NoteEditorScreenState extends State<NoteEditorScreen> {
  final _titleController = TextEditingController();
  final _descriptionController = TextEditingController();
  DateTime? _reminderDate;
  PriorityLevel _priorityLevel = PriorityLevel.normal;

  @override
  void dispose() {
    _titleController.dispose();
    _descriptionController.dispose();
    super.dispose();
  }

  Future<void> _pickDateTime() async {
    final date = await showDatePicker(
      context: context,
      initialDate: _reminderDate ?? DateTime.now(),
      firstDate: DateTime.now(),
      lastDate: DateTime.now().add(const Duration(days: 365)),
    );
    if (date == null || !mounted) return;
    final time = await showTimePicker(context: context, initialTime: TimeOfDay.now());
    if (time == null || !mounted) return;
    setState(() {
      _reminderDate = DateTime(date.year, date.month, date.day, time.hour, time.minute);
    });
  }

  void _saveNote() async {
    if (_titleController.text.isEmpty) {
      context.showErrorSnackBar('Title is required');
      return;
    }
    final now = DateTime.now();
    final note = NoteEntity(
      id: DateTime.now().millisecondsSinceEpoch.toString(),
      title: _titleController.text,
      description: _descriptionController.text,
      reminderDatetime: _reminderDate?.millisecondsSinceEpoch,
      priorityLevel: NoteEntity.priorityLevelToInt(_priorityLevel),
      isCompleted: false,
      createdAt: now.millisecondsSinceEpoch,
      updatedAt: now.millisecondsSinceEpoch,
      isSynced: false,
      isDeleted: false,
    );
    await NotesDatabase.instance.insert(note);
    if (mounted) Navigator.of(context).pop();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Edit Note'),
        actions: [
          TextButton(onPressed: _saveNote, child: const Text('Save')),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          TextField(controller: _titleController, decoration: const InputDecoration(hintText: 'Title')),
          const SizedBox(height: 16),
          TextField(controller: _descriptionController, maxLines: 5, decoration: const InputDecoration(hintText: 'Description')),
          const SizedBox(height: 24),
          Text('Priority', style: context.textTheme.titleMedium),
          const SizedBox(height: 8),
          SegmentedButton<PriorityLevel>(
            segments: const [
              ButtonSegment(value: PriorityLevel.normal, label: Text('Normal'), icon: Icon(Icons.notifications_outlined)),
              ButtonSegment(value: PriorityLevel.email, label: Text('Email'), icon: Icon(Icons.email_outlined)),
              ButtonSegment(value: PriorityLevel.alarm, label: Text('Alarm'), icon: Icon(Icons.alarm_outlined)),
            ],
            selected: {_priorityLevel},
            onSelectionChanged: (selection) => setState(() => _priorityLevel = selection.first),
          ),
          const SizedBox(height: 24),
          ListTile(
            contentPadding: EdgeInsets.zero,
            title: const Text('Reminder'),
            subtitle: Text(_reminderDate == null ? 'Not set' : '${_reminderDate!.day}/${_reminderDate!.month}/${_reminderDate!.year} ${_reminderDate!.hour.toString().padLeft(2, '0')}:${_reminderDate!.minute.toString().padLeft(2, '0')}'),
            trailing: const Icon(Icons.chevron_right),
            onTap: _pickDateTime,
          ),
        ],
      ),
    );
  }
}