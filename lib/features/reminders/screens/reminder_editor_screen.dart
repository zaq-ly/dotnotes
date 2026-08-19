import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:dotnotes/features/reminders/models/reminder.dart';
import 'package:dotnotes/features/reminders/providers/reminders_provider.dart';
import 'package:dotnotes/features/notes/models/note.dart';
import 'package:dotnotes/features/notes/providers/notes_provider.dart';
import 'package:dotnotes/shared/widgets/confirm_dialog.dart';
import 'package:dotnotes/app/theme.dart';

class ReminderEditorScreen extends ConsumerStatefulWidget {
  final String noteId;
  final String? reminderId;

  const ReminderEditorScreen({
    super.key,
    required this.noteId,
    this.reminderId,
  });

  @override
  ConsumerState<ReminderEditorScreen> createState() => _ReminderEditorScreenState();
}

class _ReminderEditorScreenState extends ConsumerState<ReminderEditorScreen> {
  DateTime _scheduledDate = DateTime.now().add(const Duration(hours: 1));
  TimeOfDay _scheduledTime = TimeOfDay.now();
  Priority _priority = Priority.normal;
  RepeatType _repeat = RepeatType.once;
  bool _alarmEnabled = true;
  bool _gmailEnabled = false;
  bool _calendarEnabled = false;
  String? _gmailRecipient;
  Reminder? _currentReminder;
  Note? _note;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _loadData();
    });
  }

  void _loadData() {
    final notesAsync = ref.read(notesProvider);
    final notes = notesAsync.valueOrNull ?? [];
    final matchingNotes = notes.where((n) => n.id == widget.noteId);
    if (matchingNotes.isNotEmpty) {
      _note = matchingNotes.first;
    }

    if (widget.reminderId != null) {
      final remindersAsync = ref.read(remindersProvider);
      final reminders = remindersAsync.valueOrNull ?? [];
      final matchingReminders = reminders.where((r) => r.id == widget.reminderId);
      if (matchingReminders.isNotEmpty) {
        _currentReminder = matchingReminders.first;
        _scheduledDate = _currentReminder!.scheduledAt;
        _scheduledTime = TimeOfDay.fromDateTime(_currentReminder!.scheduledAt);
        _priority = _currentReminder!.priority;
        _repeat = _currentReminder!.repeat;
        _alarmEnabled = _currentReminder!.alarmEnabled;
        _gmailEnabled = _currentReminder!.gmailEnabled;
        _calendarEnabled = _currentReminder!.calendarEnabled;
        _gmailRecipient = _currentReminder!.gmailRecipient;
      }
    }
    setState(() {});
  }

  Future<void> _selectDate() async {
    final picked = await showDatePicker(
      context: context,
      initialDate: _scheduledDate,
      firstDate: DateTime.now(),
      lastDate: DateTime.now().add(const Duration(days: 365)),
    );

    if (picked != null) {
      setState(() {
        _scheduledDate = picked;
      });
    }
  }

  Future<void> _selectTime() async {
    final picked = await showTimePicker(
      context: context,
      initialTime: _scheduledTime,
    );

    if (picked != null) {
      setState(() {
        _scheduledTime = picked;
      });
    }
  }

  Future<void> _saveReminder() async {
    final scheduledAt = DateTime(
      _scheduledDate.year,
      _scheduledDate.month,
      _scheduledDate.day,
      _scheduledTime.hour,
      _scheduledTime.minute,
    );

    if (scheduledAt.isBefore(DateTime.now())) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Scheduled time must be in the future')),
      );
      return;
    }

    final isEditing = _currentReminder != null && _currentReminder!.id.isNotEmpty;
    final reminder = isEditing
        ? _currentReminder!.copyWith(
            scheduledAt: scheduledAt,
            priority: _priority,
            repeat: _repeat,
            alarmEnabled: _alarmEnabled,
            gmailEnabled: _gmailEnabled,
            calendarEnabled: _calendarEnabled,
            gmailRecipient: _gmailRecipient,
          )
        : Reminder(
            id: '',
            noteId: widget.noteId,
            scheduledAt: scheduledAt,
            priority: _priority,
            repeat: _repeat,
            alarmEnabled: _alarmEnabled,
            gmailEnabled: _gmailEnabled,
            calendarEnabled: _calendarEnabled,
            gmailRecipient: _gmailRecipient,
          );

    if (isEditing) {
      await ref.read(remindersProvider.notifier).updateReminder(reminder);
    } else {
      await ref.read(remindersProvider.notifier).createReminder(reminder);
    }

    if (mounted) {
      Navigator.pop(context);
    }
  }

  Future<void> _deleteReminder() async {
    if (_currentReminder == null || _currentReminder!.id.isEmpty) return;

    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => const ConfirmDialog(
        title: 'Delete Reminder',
        message: 'Are you sure you want to delete this reminder?',
      ),
    );

    if (confirmed == true) {
      await ref.read(remindersProvider.notifier).deleteReminder(_currentReminder!.id);
      if (mounted) {
        Navigator.pop(context);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.reminderId == null ? 'New Reminder' : 'Edit Reminder'),
        actions: [
          IconButton(
            icon: const Icon(Icons.check),
            onPressed: _saveReminder,
          ),
        ],
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            if (_note != null) ...[
              Text(
                'Note',
                style: Theme.of(context).textTheme.titleSmall,
              ),
              const SizedBox(height: 8),
              Card(
                child: ListTile(
                  title: Text(_note!.title.isEmpty ? 'Untitled' : _note!.title),
                  subtitle: Text(
                    _note!.content,
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                  ),
                ),
              ),
              const SizedBox(height: 24),
            ],
            Text(
              'Schedule',
              style: Theme.of(context).textTheme.titleSmall,
            ),
            const SizedBox(height: 8),
            Row(
              children: [
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: _selectDate,
                    icon: const Icon(Icons.calendar_today),
                    label: Text(
                      '${_scheduledDate.day}/${_scheduledDate.month}/${_scheduledDate.year}',
                    ),
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: _selectTime,
                    icon: const Icon(Icons.access_time),
                    label: Text(
                      '${_scheduledTime.hour.toString().padLeft(2, '0')}:${_scheduledTime.minute.toString().padLeft(2, '0')}',
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 24),
            Text(
              'Priority',
              style: Theme.of(context).textTheme.titleSmall,
            ),
            const SizedBox(height: 8),
            Wrap(
              spacing: 8,
              children: Priority.values.map((priority) {
                return ChoiceChip(
                  label: Text(_priorityLabel(priority)),
                  selected: _priority == priority,
                  onSelected: (selected) {
                    if (selected) {
                      setState(() {
                        _priority = priority;
                      });
                    }
                  },
                );
              }).toList(),
            ),
            const SizedBox(height: 24),
            Text(
              'Repeat',
              style: Theme.of(context).textTheme.titleSmall,
            ),
            const SizedBox(height: 8),
            Wrap(
              spacing: 8,
              children: RepeatType.values.map((repeat) {
                return ChoiceChip(
                  label: Text(_repeatLabel(repeat)),
                  selected: _repeat == repeat,
                  onSelected: (selected) {
                    if (selected) {
                      setState(() {
                        _repeat = repeat;
                      });
                    }
                  },
                );
              }).toList(),
            ),
            const SizedBox(height: 24),
            SwitchListTile(
              title: const Text('Alarm'),
              subtitle: const Text('Trigger notification at scheduled time'),
              value: _alarmEnabled,
              onChanged: (value) {
                setState(() {
                  _alarmEnabled = value;
                });
              },
            ),
            SwitchListTile(
              title: const Text('Gmail'),
              subtitle: const Text('Send email reminder (Phase 3)'),
              value: _gmailEnabled,
              onChanged: (value) {
                setState(() {
                  _gmailEnabled = value;
                });
              },
            ),
            SwitchListTile(
              title: const Text('Calendar'),
              subtitle: const Text('Create calendar event (Phase 3)'),
              value: _calendarEnabled,
              onChanged: (value) {
                setState(() {
                  _calendarEnabled = value;
                });
              },
            ),
            if (widget.reminderId != null) ...[
              const SizedBox(height: 24),
              SizedBox(
                width: double.infinity,
                child: ElevatedButton.icon(
                  onPressed: _deleteReminder,
                  icon: const Icon(Icons.delete),
                  label: const Text('Delete Reminder'),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: AppColors.error,
                    foregroundColor: Colors.white,
                    padding: const EdgeInsets.symmetric(vertical: 16),
                  ),
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }

  String _priorityLabel(Priority priority) {
    switch (priority) {
      case Priority.low:
        return 'Low';
      case Priority.normal:
        return 'Normal';
      case Priority.high:
        return 'High';
      case Priority.urgent:
        return 'Urgent';
    }
  }

  String _repeatLabel(RepeatType repeat) {
    switch (repeat) {
      case RepeatType.once:
        return 'Once';
      case RepeatType.daily:
        return 'Daily';
      case RepeatType.weekly:
        return 'Weekly';
      case RepeatType.monthly:
        return 'Monthly';
    }
  }
}
