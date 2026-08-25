import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';
import '../../../app/theme/app_colors.dart';
import '../../../domain/entities/note.dart';
import '../../../l10n/app_localizations.dart';
import '../../reminder/notification_service.dart';
import '../application/note_editor_notifier.dart';

class NoteEditorScreen extends ConsumerStatefulWidget {
  final NoteEntity? initialNote;

  const NoteEditorScreen({super.key, this.initialNote});

  @override
  ConsumerState<NoteEditorScreen> createState() => _NoteEditorScreenState();
}

class _NoteEditorScreenState extends ConsumerState<NoteEditorScreen> {
  late final TextEditingController _titleController;
  late final TextEditingController _descriptionController;

  @override
  void initState() {
    super.initState();
    _titleController =
        TextEditingController(text: widget.initialNote?.title ?? '');
    _descriptionController =
        TextEditingController(text: widget.initialNote?.description ?? '');
  }

  @override
  void dispose() {
    _titleController.dispose();
    _descriptionController.dispose();
    super.dispose();
  }

  Color _getPriorityColor(PriorityLevel priority) {
    switch (priority) {
      case PriorityLevel.email:
        return AppColors.priorityEmail;
      case PriorityLevel.alarm:
        return AppColors.priorityAlarm;
      case PriorityLevel.normal:
        return AppColors.priorityNormal;
    }
  }

  String _getPriorityLabel(PriorityLevel priority, AppLocalizations l10n) {
    switch (priority) {
      case PriorityLevel.email:
        return l10n.reminderEmail;
      case PriorityLevel.alarm:
        return l10n.reminderAlarm;
      case PriorityLevel.normal:
        return l10n.reminderNormal;
    }
  }

  Future<void> _openReminderBottomSheet(
    BuildContext context,
    NoteEditorState state,
    NoteEditorNotifier notifier,
  ) async {
    final l10n = AppLocalizations.of(context)!;
    final notifService = ref.read(notificationServiceProvider);
    await notifService.requestPermissions();

    DateTime selectedDate =
        state.reminderDateTime ?? DateTime.now().add(const Duration(hours: 1));
    TimeOfDay selectedTime = TimeOfDay.fromDateTime(selectedDate);
    PriorityLevel selectedPriority = state.priorityLevel;

    if (!context.mounted) return;

    await showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(16)),
      ),
      builder: (ctx) {
        return StatefulBuilder(
          builder: (modalCtx, setModalState) {
            final theme = Theme.of(modalCtx);
            final combinedDateTime = DateTime(
              selectedDate.year,
              selectedDate.month,
              selectedDate.day,
              selectedTime.hour,
              selectedTime.minute,
            );

            return SafeArea(
              child: Padding(
                padding: const EdgeInsets.fromLTRB(20, 16, 20, 20),
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text(
                          l10n.setReminder,
                          style: theme.textTheme.titleMedium?.copyWith(
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                        if (state.reminderDateTime != null)
                          TextButton(
                            onPressed: () {
                              notifier.removeReminder();
                              Navigator.pop(modalCtx);
                            },
                            child: Text(
                              l10n.removeReminder,
                              style: const TextStyle(color: Colors.redAccent),
                            ),
                          ),
                      ],
                    ),
                    const SizedBox(height: 16),

                    // Date & Time selectors
                    Row(
                      children: [
                        Expanded(
                          child: OutlinedButton.icon(
                            icon: const Icon(Icons.calendar_today, size: 16),
                            label: Text(
                              DateFormat('dd MMM yyyy').format(selectedDate),
                            ),
                            onPressed: () async {
                              final pickedDate = await showDatePicker(
                                context: modalCtx,
                                initialDate: selectedDate,
                                firstDate: DateTime.now(),
                                lastDate: DateTime.now().add(
                                  const Duration(days: 365 * 5),
                                ),
                              );
                              if (pickedDate != null) {
                                setModalState(() {
                                  selectedDate = pickedDate;
                                });
                              }
                            },
                          ),
                        ),
                        const SizedBox(width: 8),
                        Expanded(
                          child: OutlinedButton.icon(
                            icon: const Icon(Icons.access_time, size: 16),
                            label: Text(
                              selectedTime.format(modalCtx),
                            ),
                            onPressed: () async {
                              final pickedTime = await showTimePicker(
                                context: modalCtx,
                                initialTime: selectedTime,
                              );
                              if (pickedTime != null) {
                                setModalState(() {
                                  selectedTime = pickedTime;
                                });
                              }
                            },
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 16),

                    // Priority Level Selector
                    Text(
                      l10n.priority,
                      style: theme.textTheme.bodyMedium?.copyWith(
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                    const SizedBox(height: 8),
                    RadioGroup<PriorityLevel>(
                      groupValue: selectedPriority,
                      onChanged: (p) {
                        if (p != null) {
                          setModalState(() {
                            selectedPriority = p;
                          });
                        }
                      },
                      child: Column(
                        children: [
                          RadioListTile<PriorityLevel>(
                            title: Text(l10n.reminderNormal),
                            value: PriorityLevel.normal,
                            dense: true,
                            contentPadding: EdgeInsets.zero,
                          ),
                          RadioListTile<PriorityLevel>(
                            title: Text(l10n.reminderEmail),
                            value: PriorityLevel.email,
                            dense: true,
                            contentPadding: EdgeInsets.zero,
                          ),
                          RadioListTile<PriorityLevel>(
                            title: Text(l10n.reminderAlarm),
                            value: PriorityLevel.alarm,
                            dense: true,
                            contentPadding: EdgeInsets.zero,
                          ),
                        ],
                      ),
                    ),
                    const SizedBox(height: 16),

                    // Save Reminder button
                    SizedBox(
                      width: double.infinity,
                      child: FilledButton(
                        onPressed: () {
                          notifier.updateReminder(
                            combinedDateTime,
                            selectedPriority,
                          );
                          Navigator.pop(modalCtx);
                        },
                        child: Text(l10n.done),
                      ),
                    ),
                  ],
                ),
              ),
            );
          },
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    final theme = Theme.of(context);
    final state = ref.watch(noteEditorNotifierProvider(widget.initialNote));
    final notifier =
        ref.read(noteEditorNotifierProvider(widget.initialNote).notifier);

    final hasReminder = state.reminderDateTime != null;

    return PopScope(
      canPop: true,
      onPopInvokedWithResult: (didPop, _) {
        if (didPop) {
          notifier.saveNote();
        }
      },
      child: Scaffold(
        appBar: AppBar(
          title: Text(
            state.isNew ? l10n.newNote : l10n.editNote,
            style: theme.textTheme.titleMedium,
          ),
          actions: [
            IconButton(
              icon: Icon(
                hasReminder
                    ? Icons.notifications_active
                    : Icons.notifications_none,
                color: hasReminder
                    ? _getPriorityColor(state.priorityLevel)
                    : null,
              ),
              tooltip: l10n.reminder,
              onPressed: () =>
                  _openReminderBottomSheet(context, state, notifier),
            ),
            if (!state.isNew)
              IconButton(
                icon: const Icon(Icons.delete_outline),
                tooltip: l10n.delete,
                onPressed: () async {
                  final confirmed = await showDialog<bool>(
                    context: context,
                    builder: (ctx) => AlertDialog(
                      title: Text(l10n.delete),
                      content: Text(
                        '${l10n.delete} "${state.title.isEmpty ? 'Untitled' : state.title}"?',
                      ),
                      actions: [
                        TextButton(
                          onPressed: () => Navigator.pop(ctx, false),
                          child: Text(l10n.cancel),
                        ),
                        TextButton(
                          onPressed: () => Navigator.pop(ctx, true),
                          child: Text(
                            l10n.delete,
                            style: const TextStyle(color: Colors.redAccent),
                          ),
                        ),
                      ],
                    ),
                  );

                  if (confirmed == true && context.mounted) {
                    await notifier.deleteCurrentNote();
                    if (context.mounted) {
                      Navigator.pop(context);
                    }
                  }
                },
              ),
            IconButton(
              icon: const Icon(Icons.check),
              tooltip: l10n.save,
              onPressed: state.isSaving
                  ? null
                  : () async {
                      final saved = await notifier.saveNote();
                      if (saved && context.mounted) {
                        Navigator.pop(context);
                      }
                    },
            ),
          ],
        ),
        body: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
                    // Active Reminder Pill Badge
                    if (hasReminder) ...[
                      InkWell(
                        onTap: () =>
                            _openReminderBottomSheet(context, state, notifier),
                        borderRadius: BorderRadius.circular(20),
                        child: Container(
                          padding: const EdgeInsets.symmetric(
                            horizontal: 10,
                            vertical: 5,
                          ),
                          decoration: BoxDecoration(
                            color: _getPriorityColor(state.priorityLevel)
                                .withAlpha(30),
                            borderRadius: BorderRadius.circular(20),
                            border: Border.all(
                              color: _getPriorityColor(state.priorityLevel)
                                  .withAlpha(120),
                            ),
                          ),
                          child: Row(
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              Icon(
                                state.priorityLevel == PriorityLevel.alarm
                                    ? Icons.alarm
                                    : Icons.notifications,
                                size: 14,
                                color: _getPriorityColor(state.priorityLevel),
                              ),
                              const SizedBox(width: 6),
                              Text(
                                '${DateFormat('dd MMM yyyy, HH:mm').format(state.reminderDateTime!)} (${_getPriorityLabel(state.priorityLevel, l10n)})',
                                style: theme.textTheme.bodySmall?.copyWith(
                                  color: theme.textTheme.bodyMedium?.color,
                                  fontWeight: FontWeight.w500,
                                ),
                              ),
                              const SizedBox(width: 4),
                              GestureDetector(
                                onTap: notifier.removeReminder,
                                child: const Icon(Icons.close, size: 14),
                              ),
                            ],
                          ),
                        ),
                      ),
                      const SizedBox(height: 12),
                    ],

                    TextField(
                      controller: _titleController,
                      style: theme.textTheme.headlineMedium?.copyWith(
                        fontWeight: FontWeight.w600,
                      ),
                      decoration: InputDecoration(
                        hintText: l10n.titlePlaceholder,
                        border: InputBorder.none,
                        enabledBorder: InputBorder.none,
                        focusedBorder: InputBorder.none,
                        contentPadding: EdgeInsets.zero,
                      ),
                      maxLines: null,
                      textCapitalization: TextCapitalization.sentences,
                      onChanged: notifier.updateTitle,
                    ),
                    const SizedBox(height: 12),
                    Expanded(
                      child: TextField(
                        controller: _descriptionController,
                        style: theme.textTheme.bodyMedium?.copyWith(
                          fontSize: 15,
                          height: 1.6,
                        ),
                        decoration: InputDecoration(
                          hintText: l10n.descriptionPlaceholder,
                          border: InputBorder.none,
                          enabledBorder: InputBorder.none,
                          focusedBorder: InputBorder.none,
                          contentPadding: EdgeInsets.zero,
                        ),
                        maxLines: null,
                        expands: true,
                        textAlignVertical: TextAlignVertical.top,
                        textCapitalization: TextCapitalization.sentences,
                        onChanged: notifier.updateDescription,
                      ),
                    ),
                  ],
                ),
              ),
      ),
    );
  }
}
