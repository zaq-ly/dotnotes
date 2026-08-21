import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/extensions/extensions.dart';
import '../../../data/local/notes_database.dart';
import '../../../data/models/note_entity.dart';
import '../application/note_editor_provider.dart';

class NoteEditorScreen extends ConsumerStatefulWidget {
  const NoteEditorScreen({super.key});

  @override
  ConsumerState<NoteEditorScreen> createState() => _NoteEditorScreenState();
}

class _NoteEditorScreenState extends ConsumerState<NoteEditorScreen> {
  final _titleController = TextEditingController();
  final _descriptionController = TextEditingController();
  DateTime? _reminderDate;
  int _priorityLevel = 1;
  bool _isNew = true;

  @override
  void initState() {
    super.initState();
    _loadNote();
  }

  void _loadNote() {
    final note = ref.read(noteEditorProvider);
    if (note != null) {
      _titleController.text = note.title;
      _descriptionController.text = note.description;
      _priorityLevel = note.priorityLevel;
      _reminderDate = note.reminderDatetime != null ? DateTime.fromMillisecondsSinceEpoch(note.reminderDatetime!) : null;
      _isNew = note.title.isEmpty;
    }
  }

  @override
  void dispose() {
    _titleController.dispose();
    _descriptionController.dispose();
    super.dispose();
  }

  Future<void> _pickDateTime() async {
    final date = await showDatePicker(context: context, initialDate: _reminderDate ?? DateTime.now(), firstDate: DateTime.now(), lastDate: DateTime.now().add(const Duration(days: 365)));
    if (date == null || !mounted) return;
    final time = await showTimePicker(context: context, initialTime: TimeOfDay.now());
    if (time == null || !mounted) return;
    setState(() => _reminderDate = DateTime(date.year, date.month, date.day, time.hour, time.minute));
  }

  void _saveNote() async {
    if (_titleController.text.isEmpty) {
      context.showErrorSnackBar('Title is required');
      return;
    }
    final existing = ref.read(noteEditorProvider);
    final note = NoteEntity(
      id: existing?.id ?? DateTime.now().millisecondsSinceEpoch.toString(),
      title: _titleController.text,
      description: _descriptionController.text,
      reminderDatetime: _reminderDate?.millisecondsSinceEpoch,
      priorityLevel: _priorityLevel,
      isCompleted: existing?.isCompleted ?? false,
      createdAt: existing?.createdAt ?? DateTime.now().millisecondsSinceEpoch,
      updatedAt: DateTime.now().millisecondsSinceEpoch,
      isSynced: false,
      isDeleted: false,
    );
    final notifier = ref.read(noteEditorProvider.notifier);
    notifier.setNote(note);
    if (_isNew) {
      await notifier.save();
    } else {
      await notifier.update();
    }
    if (mounted) Navigator.of(context).pop();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Edit Note'), actions: [TextButton(onPressed: _saveNote, child: const Text('Save'))]),
      body: ListView(padding: const EdgeInsets.all(16), children: [
        TextField(controller: _titleController, decoration: const InputDecoration(hintText: 'Title')),
        const SizedBox(height: 16),
        TextField(controller: _descriptionController, maxLines: 5, decoration: const InputDecoration(hintText: 'Description')),
        const SizedBox(height: 24),
        Text('Priority', style: context.textTheme.titleMedium),
        const SizedBox(height: 8),
        SegmentedButton<int>(segments: const [ButtonSegment(value: 1, label: Text('Normal'), icon: Icon(Icons.notifications_outlined)), ButtonSegment(value: 2, label: Text('Email'), icon: Icon(Icons.email_outlined)), ButtonSegment(value: 3, label: Text('Alarm'), icon: Icon(Icons.alarm_outlined))], selected: {_priorityLevel}, onSelectionChanged: (selection) => setState(() => _priorityLevel = selection.first)),
        const SizedBox(height: 24),
        ListTile(contentPadding: EdgeInsets.zero, title: const Text('Reminder'), subtitle: Text(_reminderDate == null ? 'Not set' : '${_reminderDate!.day}/${_reminderDate!.month}/${_reminderDate!.year} ${_reminderDate!.hour.toString().padLeft(2, '0')}:${_reminderDate!.minute.toString().padLeft(2, '0')}'), trailing: const Icon(Icons.chevron_right), onTap: _pickDateTime),
      ]),
    );
  }
}