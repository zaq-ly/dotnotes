import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:dotnotes/features/notes/models/note.dart';
import 'package:dotnotes/features/notes/providers/notes_provider.dart';
import 'package:dotnotes/shared/widgets/confirm_dialog.dart';
import 'package:dotnotes/app/theme.dart';
import 'package:dotnotes/app/routes.dart';

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
            icon: const Icon(Icons.check),
            onPressed: _saveNote,
          ),
        ],
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            TextField(
              controller: _titleController,
              decoration: const InputDecoration(
                hintText: AppStrings.noteTitleHint,
                border: InputBorder.none,
                filled: false,
              ),
              style: const TextStyle(
                fontSize: 24,
                fontWeight: FontWeight.bold,
              ),
              maxLines: null,
            ),
            const SizedBox(height: 16),
            TextField(
              controller: _contentController,
              decoration: const InputDecoration(
                hintText: AppStrings.noteContentHint,
                border: InputBorder.none,
                filled: false,
              ),
              style: const TextStyle(fontSize: 16),
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
                  decoration: const InputDecoration(
                    labelText: AppStrings.categoryHint,
                    prefixIcon: Icon(Icons.label),
                  ),
                );
              },
            ),
            const SizedBox(height: 16),
            Row(
              children: [
                Expanded(
                  child: CheckboxListTile(
                    title: const Text('Pinned'),
                    value: _isPinned,
                    onChanged: (value) {
                      setState(() {
                        _isPinned = value ?? false;
                      });
                    },
                    controlAffinity: ListTileControlAffinity.leading,
                  ),
                ),
                Expanded(
                  child: CheckboxListTile(
                    title: const Text('Archived'),
                    value: _isArchived,
                    onChanged: (value) {
                      setState(() {
                        _isArchived = value ?? false;
                      });
                    },
                    controlAffinity: ListTileControlAffinity.leading,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 24),
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
                      const SnackBar(content: Text('Save note first to add reminder')),
                    );
                  }
                },
                icon: const Icon(Icons.alarm_add),
                label: const Text('Add Reminder'),
                style: OutlinedButton.styleFrom(
                  padding: const EdgeInsets.symmetric(vertical: 16),
                ),
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
}
