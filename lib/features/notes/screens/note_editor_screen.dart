import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:dotnotes/app/theme.dart';
import 'package:dotnotes/app/routes.dart';
import 'package:dotnotes/features/notes/models/note.dart';
import 'package:dotnotes/features/notes/providers/notes_provider.dart';
import 'package:dotnotes/shared/widgets/confirm_dialog.dart';
import 'package:dotnotes/shared/widgets/paper_card.dart';

class NoteEditorScreen extends ConsumerStatefulWidget {
  final String? noteId;

  const NoteEditorScreen({super.key, this.noteId});

  @override
  ConsumerState<NoteEditorScreen> createState() => _NoteEditorScreenState();
}

class _NoteEditorScreenState extends ConsumerState<NoteEditorScreen> {
  late TextEditingController _titleController;
  late TextEditingController _contentController;
  late TextEditingController _categoryController;
  bool _isPinned = false;
  bool _isArchived = false;
  Note? _currentNote;

  @override
  void initState() {
    super.initState();
    _titleController = TextEditingController();
    _contentController = TextEditingController();
    _categoryController = TextEditingController();

    WidgetsBinding.instance.addPostFrameCallback((_) {
      _loadNote();
    });
  }

  void _loadNote() {
    if (widget.noteId != null) {
      final notesAsync = ref.read(notesProvider);
      final notes = notesAsync.valueOrNull ?? [];
      final matches = notes.where((note) => note.id == widget.noteId);
      if (matches.isNotEmpty) {
        _currentNote = matches.first;
        _titleController.text = _currentNote!.title;
        _contentController.text = _currentNote!.content;
        _categoryController.text = _currentNote!.category ?? '';
        _isPinned = _currentNote!.isPinned;
        _isArchived = _currentNote!.isArchived;
        setState(() {});
      }
    }
  }

  @override
  void dispose() {
    _titleController.dispose();
    _contentController.dispose();
    _categoryController.dispose();
    super.dispose();
  }

  Future<void> _saveNote() async {
    final title = _titleController.text.trim();
    final content = _contentController.text.trim();

    if (title.isEmpty && content.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Note cannot be empty')),
      );
      return;
    }

    final isEditing = _currentNote != null && _currentNote!.id.isNotEmpty;
    final note = isEditing
        ? _currentNote!.copyWith(
            title: title,
            content: content,
            category: _categoryController.text.trim().isEmpty
                ? null
                : _categoryController.text.trim(),
            isPinned: _isPinned,
            isArchived: _isArchived,
          )
        : Note(
            id: '',
            userId: 'default_user',
            title: title,
            content: content,
            category: _categoryController.text.trim().isEmpty
                ? null
                : _categoryController.text.trim(),
            isPinned: _isPinned,
            isArchived: _isArchived,
            createdAt: DateTime.now(),
            updatedAt: DateTime.now(),
          );

    if (isEditing) {
      await ref.read(notesProvider.notifier).updateNote(note);
    } else {
      await ref.read(notesProvider.notifier).createNote(note);
    }

    if (mounted) {
      Navigator.pop(context);
    }
  }

  Future<void> _deleteNote() async {
    if (_currentNote == null || _currentNote!.id.isEmpty) return;

    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => const ConfirmDialog(
        title: AppStrings.confirmDeleteTitle,
        message: AppStrings.confirmDeleteMessage,
      ),
    );

    if (confirmed == true) {
      await ref.read(notesProvider.notifier).deleteNote(_currentNote!.id);
      if (mounted) {
        Navigator.pop(context);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final categories = ref.watch(categoriesProvider);

    return Scaffold(
      appBar: AppBar(
        title: Text(widget.noteId == null ? 'New Note' : 'Edit Note'),
        actions: [
          IconButton(
            tooltip: 'Save note',
            icon: const Icon(Icons.check),
            onPressed: _saveNote,
          ),
        ],
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.fromLTRB(16, 8, 16, 32),
        child: Column(
          children: [
            PaperCard(
              padding: const EdgeInsets.fromLTRB(16, 18, 16, 16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  TextField(
                    controller: _titleController,
                    decoration: const InputDecoration(
                      hintText: AppStrings.noteTitleHint,
                      border: InputBorder.none,
                      filled: false,
                      hintStyle: TextStyle(
                        fontFamily: AppFonts.mono,
                        fontSize: 24,
                        color: AppColors.inkSoft,
                      ),
                    ),
                    style: const TextStyle(
                      fontFamily: AppFonts.body,
                      fontSize: 24,
                      fontWeight: FontWeight.w700,
                      color: AppColors.ink,
                    ),
                    maxLines: null,
                  ),
                  const SizedBox(height: 14),
                  Container(
                    height: 1,
                    color: Colors.black.withValues(alpha: 0.08),
                  ),
                  const SizedBox(height: 14),
                  TextField(
                    controller: _contentController,
                    decoration: const InputDecoration(
                      hintText: AppStrings.noteContentHint,
                      border: InputBorder.none,
                      filled: false,
                      hintStyle: TextStyle(
                        fontFamily: AppFonts.mono,
                        fontSize: 15,
                        color: AppColors.inkSoft,
                      ),
                    ),
                    style: const TextStyle(
                      fontFamily: AppFonts.body,
                      fontSize: 15,
                      height: 1.55,
                      color: AppColors.ink,
                    ),
                    maxLines: null,
                    minLines: 10,
                  ),
                  const SizedBox(height: 16),
                  Autocomplete<String>(
                    optionsBuilder: (TextEditingValue textEditingValue) {
                      if (textEditingValue.text.isEmpty) {
                        return const Iterable<String>.empty();
                      }
                      return categories.where((String option) {
                        return option
                            .toLowerCase()
                            .contains(textEditingValue.text.toLowerCase());
                      });
                    },
                    onSelected: (String selection) {
                      _categoryController.text = selection;
                    },
                    fieldViewBuilder: (
                      BuildContext context,
                      TextEditingController fieldTextEditingController,
                      FocusNode fieldFocusNode,
                      VoidCallback onFieldSubmitted,
                    ) {
                      if (_categoryController.text.isNotEmpty &&
                          fieldTextEditingController.text.isEmpty) {
                        fieldTextEditingController.text = _categoryController.text;
                      }

                      fieldTextEditingController.addListener(() {
                        _categoryController.text = fieldTextEditingController.text;
                      });

                      return TextField(
                        controller: fieldTextEditingController,
                        focusNode: fieldFocusNode,
                        decoration: InputDecoration(
                          labelText: AppStrings.categoryHint,
                          prefixIcon: const Icon(
                            Icons.label,
                            size: 18,
                            color: AppColors.inkSoft,
                          ),
                          labelStyle: const TextStyle(
                            fontFamily: AppFonts.mono,
                            fontSize: 12,
                            fontWeight: FontWeight.w700,
                            letterSpacing: 0.5,
                            color: AppColors.inkSoft,
                          ),
                          filled: true,
                          fillColor: Colors.black.withValues(alpha: 0.06),
                          contentPadding: const EdgeInsets.symmetric(
                            horizontal: 12,
                            vertical: 12,
                          ),
                          border: OutlineInputBorder(
                            borderRadius: BorderRadius.circular(10),
                            borderSide: BorderSide.none,
                          ),
                          enabledBorder: OutlineInputBorder(
                            borderRadius: BorderRadius.circular(10),
                            borderSide: BorderSide.none,
                          ),
                          focusedBorder: OutlineInputBorder(
                            borderRadius: BorderRadius.circular(10),
                            borderSide: const BorderSide(
                              color: AppColors.signal,
                              width: 1.5,
                            ),
                          ),
                        ),
                        style: const TextStyle(
                          fontFamily: AppFonts.body,
                          fontSize: 14,
                          color: AppColors.ink,
                        ),
                      );
                    },
                  ),
                  const SizedBox(height: 8),
                  Row(
                    children: [
                      Expanded(
                        child: _PaperToggle(
                          label: 'PINNED',
                          value: _isPinned,
                          onChanged: (value) {
                            setState(() {
                              _isPinned = value;
                            });
                          },
                        ),
                      ),
                      Expanded(
                        child: _PaperToggle(
                          label: 'ARCHIVED',
                          value: _isArchived,
                          onChanged: (value) {
                            setState(() {
                              _isArchived = value;
                            });
                          },
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
            const SizedBox(height: 20),
            SizedBox(
              width: double.infinity,
              child: OutlinedButton.icon(
                onPressed: () {
                  if (widget.noteId != null && widget.noteId!.isNotEmpty) {
                    Navigator.pushNamed(
                      context,
                      AppRoutes.reminderEditor,
                      arguments: {'noteId': widget.noteId},
                    );
                  } else {
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(
                        content: Text('Save note first to add reminder'),
                      ),
                    );
                  }
                },
                icon: const Icon(Icons.alarm_add),
                label: const Text('Add Reminder'),
              ),
            ),
            if (widget.noteId != null) ...[
              const SizedBox(height: 8),
              SizedBox(
                width: double.infinity,
                child: ElevatedButton.icon(
                  onPressed: _deleteNote,
                  icon: const Icon(Icons.delete),
                  label: const Text(AppStrings.deleteButton),
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
}

class _PaperToggle extends StatelessWidget {
  final String label;
  final bool value;
  final ValueChanged<bool> onChanged;

  const _PaperToggle({
    required this.label,
    required this.value,
    required this.onChanged,
  });

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: () => onChanged(!value),
      borderRadius: BorderRadius.circular(10),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 10),
        child: Row(
          children: [
            Container(
              width: 10,
              height: 10,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                color: value ? AppColors.signal : Colors.transparent,
                border: Border.all(
                  color: value ? AppColors.signal : AppColors.inkSoft,
                  width: 1.5,
                ),
              ),
            ),
            const SizedBox(width: 8),
            Text(
              label,
              style: const TextStyle(
                fontFamily: AppFonts.mono,
                fontSize: 11,
                fontWeight: FontWeight.w700,
                letterSpacing: 1.2,
                color: AppColors.ink,
              ),
            ),
          ],
        ),
      ),
    );
  }
}