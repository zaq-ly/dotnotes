import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../domain/entities/note.dart';
import '../../../l10n/app_localizations.dart';
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
    _titleController = TextEditingController(text: widget.initialNote?.title ?? '');
    _descriptionController =
        TextEditingController(text: widget.initialNote?.description ?? '');
  }

  @override
  void dispose() {
    _titleController.dispose();
    _descriptionController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    final theme = Theme.of(context);
    final state = ref.watch(noteEditorNotifierProvider(widget.initialNote));
    final notifier = ref.read(noteEditorNotifierProvider(widget.initialNote).notifier);

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
        body: state.isSaving
            ? const Center(child: CircularProgressIndicator())
            : Padding(
                padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
                child: Column(
                  children: [
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
