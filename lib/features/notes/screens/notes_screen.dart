import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:dotnotes/app/theme.dart';
import 'package:dotnotes/features/notes/providers/notes_provider.dart';
import 'package:dotnotes/features/notes/widgets/note_card.dart';

final searchQueryProvider = StateProvider<String>((ref) => '');
final tabIndexProvider = StateProvider<int>((ref) => 0);

class NotesScreen extends ConsumerWidget {
  const NotesScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final notesAsync = ref.watch(notesProvider);
    final searchQuery = ref.watch(searchQueryProvider);
    final tabIndex = ref.watch(tabIndexProvider);

    return DefaultTabController(
      length: 3,
      child: Scaffold(
        appBar: AppBar(
          title: Padding(
            padding: const EdgeInsets.only(bottom: 2),
            child: TextField(
              decoration: InputDecoration(
                hintText: AppStrings.searchPlaceholder,
                prefixIcon: const Icon(Icons.search),
                suffixIcon: searchQuery.isNotEmpty
                    ? IconButton(
                        icon: const Icon(Icons.clear),
                        onPressed: () {
                          ref.read(searchQueryProvider.notifier).state = '';
                        },
                      )
                    : null,
              ),
              onChanged: (value) {
                ref.read(searchQueryProvider.notifier).state = value;
              },
            ),
          ),
          bottom: TabBar(
            onTap: (index) {
              ref.read(tabIndexProvider.notifier).state = index;
            },
            tabs: const [
              Tab(text: 'All'),
              Tab(text: 'Pinned'),
              Tab(text: 'Archived'),
            ],
          ),
        ),
        body: notesAsync.when(
          data: (allNotes) {
            final filteredNotes = _getFilteredNotes(
              allNotes,
              tabIndex,
              searchQuery,
              ref,
            );

            if (filteredNotes.isEmpty) {
              return _buildEmptyState(context);
            }

            return RefreshIndicator(
              color: AppColors.signal,
              backgroundColor: AppColors.raise,
              onRefresh: () async {
                await ref.read(notesProvider.notifier).loadNotes();
              },
              child: ListView.builder(
                padding: const EdgeInsets.fromLTRB(16, 16, 16, 96),
                itemCount: filteredNotes.length,
                itemBuilder: (context, index) {
                  final note = filteredNotes[index];
                  return NoteCard(
                    note: note,
                    onTap: () {
                      Navigator.pushNamed(
                        context,
                        '/note-editor',
                        arguments: {'noteId': note.id},
                      );
                    },
                  );
                },
              ),
            );
          },
          loading: () => const Center(child: CircularProgressIndicator()),
          error: (error, stack) => Center(
            child: Text('Error: $error'),
          ),
        ),
        floatingActionButton: FloatingActionButton(
          tooltip: 'New note',
          onPressed: () {
            Navigator.pushNamed(context, '/note-editor');
          },
          child: const Icon(Icons.add),
        ),
      ),
    );
  }

  List _getFilteredNotes(
    List notes,
    int tabIndex,
    String searchQuery,
    WidgetRef ref,
  ) {
    List baseNotes;

    switch (tabIndex) {
      case 1:
        baseNotes = ref.read(pinnedNotesProvider);
        break;
      case 2:
        baseNotes = ref.read(archivedNotesProvider);
        break;
      default:
        baseNotes = ref.read(activeNotesProvider);
    }

    if (searchQuery.isEmpty) {
      return baseNotes;
    }

    final lowerQuery = searchQuery.toLowerCase();
    return baseNotes.where((note) {
      return note.title.toLowerCase().contains(lowerQuery) ||
          note.content.toLowerCase().contains(lowerQuery);
    }).toList();
  }

  Widget _buildEmptyState(BuildContext context) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Container(
            width: 28,
            height: 28,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              border: Border.all(color: AppColors.line, width: 1.5),
            ),
          ),
          const SizedBox(height: 20),
          const Text(
            AppStrings.emptyStateTitle,
            style: TextStyle(
              fontFamily: AppFonts.body,
              fontSize: 18,
              fontWeight: FontWeight.w600,
              color: AppColors.paper,
            ),
          ),
          const SizedBox(height: 8),
          const Text(
            AppStrings.emptyStateSubtitle,
            style: TextStyle(
              fontFamily: AppFonts.mono,
              fontSize: 13,
              color: AppColors.muted,
            ),
          ),
        ],
      ),
    );
  }
}