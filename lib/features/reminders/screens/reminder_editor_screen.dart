import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:dotnotes/app/theme.dart';
import 'package:dotnotes/features/notes/models/note.dart';
import 'package:dotnotes/features/notes/providers/notes_provider.dart';
import 'package:dotnotes/features/reminders/models/reminder.dart';
import 'package:dotnotes/features/reminders/providers/reminders_provider.dart';
import 'package:dotnotes/shared/widgets/brand.dart';
import 'package:dotnotes/shared/widgets/confirm_dialog.dart';
import 'package:dotnotes/shared/widgets/paper_card.dart';

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
            tooltip: 'Save reminder',
            icon: const Icon(Icons.check),
            onPressed: _saveReminder,
          ),
        ],
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.fromLTRB(16, 8, 16, 32),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            if (_note != null) ...[
              const _SectionHeader(text: 'Note', count: 1, active: true),
              const SizedBox(height: 12),
              PaperCard(
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      _note!.title.isEmpty ? 'Untitled' : _note!.title,
                      style: const TextStyle(
                        fontFamily: AppFonts.body,
                        fontSize: 16,
                        fontWeight: FontWeight.w700,
                        color: AppColors.ink,
                      ),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                    if (_note!.content.isNotEmpty) ...[
                      const SizedBox(height: 4),
                      Text(
                        _note!.content,
                        style: const TextStyle(
                          fontFamily: AppFonts.body,
                          fontSize: 13,
                          height: 1.4,
                          color: AppColors.inkSoft,
                        ),
                        maxLines: 2,
                        overflow: TextOverflow.ellipsis,
                      ),
                    ],
                  ],
                ),
              ),
              const SizedBox(height: 24),
            ],
            const _SectionHeader(text: 'Schedule', count: 1, active: true),
            const SizedBox(height: 12),
            Row(
              children: [
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: _selectDate,
                    icon: const Icon(Icons.calendar_today, size: 16),
                    label: Text(
                      '${_scheduledDate.day.toString().padLeft(2, '0')}.${_scheduledDate.month.toString().padLeft(2, '0')}.${_scheduledDate.year}',
                      style: const TextStyle(
                        fontFamily: AppFonts.mono,
                        fontSize: 12,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                  ),
                ),
                const SizedBox(width: 10),
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: _selectTime,
                    icon: const Icon(Icons.access_time, size: 16),
                    label: Text(
                      '${_scheduledTime.hour.toString().padLeft(2, '0')}:${_scheduledTime.minute.toString().padLeft(2, '0')}',
                      style: const TextStyle(
                        fontFamily: AppFonts.mono,
                        fontSize: 12,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 24),
            const _SectionHeader(text: 'Priority', count: 1, active: true),
            const SizedBox(height: 12),
            _PriorityMeter(
              priority: _priority,
              onChanged: (priority) {
                setState(() {
                  _priority = priority;
                });
              },
            ),
            const SizedBox(height: 6),
            Padding(
              padding: const EdgeInsets.only(left: 2),
              child: Text(
                _priorityMode(_priority),
                style: const TextStyle(
                  fontFamily: AppFonts.mono,
                  fontSize: 11,
                  letterSpacing: 1.2,
                  color: AppColors.muted,
                ),
              ),
            ),
            const SizedBox(height: 24),
            const _SectionHeader(text: 'Repeat', count: 1, active: true),
            const SizedBox(height: 12),
            Wrap(
              spacing: 8,
              runSpacing: 8,
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
              contentPadding: EdgeInsets.zero,
              title: const Text('Alarm'),
              subtitle: const Text('Ring at the scheduled time'),
              value: _alarmEnabled,
              onChanged: (value) {
                setState(() {
                  _alarmEnabled = value;
                });
              },
            ),
            SwitchListTile(
              contentPadding: EdgeInsets.zero,
              title: const Text('Gmail'),
              subtitle: const Text('Send an email when it fires'),
              value: _gmailEnabled,
              onChanged: (value) {
                setState(() {
                  _gmailEnabled = value;
                });
              },
            ),
            SwitchListTile(
              contentPadding: EdgeInsets.zero,
              title: const Text('Calendar'),
              subtitle: const Text('Add the moment to your calendar'),
              value: _calendarEnabled,
              onChanged: (value) {
                setState(() {
                  _calendarEnabled = value;
                });
              },
            ),
            if (widget.reminderId != null) ...[
              const SizedBox(height: 16),
              SizedBox(
                width: double.infinity,
                child: ElevatedButton.icon(
                  onPressed: _deleteReminder,
                  icon: const Icon(Icons.delete),
                  label: const Text('Delete Reminder'),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: AppColors.danger,
                    foregroundColor: AppColors.paper,
                  ),
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }

  String _priorityMode(Priority priority) {
    switch (priority) {
      case Priority.low:
        return 'SILENT';
      case Priority.normal:
        return 'ALARM';
      case Priority.high:
        return 'ALARM + NOTIFICATION';
      case Priority.urgent:
        return 'ALARM + GMAIL + CALENDAR';
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

class _SectionHeader extends StatelessWidget {
  final String text;
  final int count;
  final bool active;

  const _SectionHeader({
    required this.text,
    required this.count,
    required this.active,
  });

  @override
  Widget build(BuildContext context) {
    return SectionLabel(text: text, count: count, active: active);
  }
}

class _PriorityMeter extends StatelessWidget {
  final Priority priority;
  final ValueChanged<Priority> onChanged;

  const _PriorityMeter({
    required this.priority,
    required this.onChanged,
  });

  String get _label {
    switch (priority) {
      case Priority.low:
        return 'LOW';
      case Priority.normal:
        return 'NORMAL';
      case Priority.high:
        return 'HIGH';
      case Priority.urgent:
        return 'URGENT';
    }
  }

  @override
  Widget build(BuildContext context) {
    final level = priority.index;

    return Row(
      children: [
        for (int i = 0; i < Priority.values.length; i++)
          GestureDetector(
            onTap: () => onChanged(Priority.values[i]),
            child: Padding(
              padding: const EdgeInsets.only(right: 10),
              child: Container(
                width: 30,
                height: 30,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  color: i <= level
                      ? AppColors.signal.withValues(alpha: 0.16)
                      : Colors.transparent,
                  border: Border.all(
                    color: i <= level ? AppColors.signal : AppColors.line,
                    width: 1.5,
                  ),
                ),
                alignment: Alignment.center,
                child: Container(
                  width: 9,
                  height: 9,
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    color: i <= level ? AppColors.signal : AppColors.line,
                  ),
                ),
              ),
            ),
          ),
        const SizedBox(width: 8),
        Text(
          _label,
          style: const TextStyle(
            fontFamily: AppFonts.mono,
            fontSize: 12,
            fontWeight: FontWeight.w700,
            letterSpacing: 1,
            color: AppColors.signal,
          ),
        ),
      ],
    );
  }
}