import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:dotnotes/features/home/providers/home_provider.dart';
import 'package:dotnotes/features/notes/models/note.dart';
import 'package:dotnotes/features/notes/screens/notes_screen.dart';
import 'package:dotnotes/app/theme.dart';

class HomeScreen extends ConsumerWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final homeDataAsync = ref.watch(homeDataProvider);

    final recentNotes = homeDataAsync.valueOrNull?.recentNotes ?? [];

    void _goToNotes() {
      Navigator.pushNamed(context, '/notes');
    }

    return Scaffold(
      appBar: AppBar(
        title: Text(AppStrings.appName),
        actions: [
          IconButton(
            icon: const Icon(Icons.search),
            onPressed: () {},
          ),
          IconButton(
            icon: const Icon(Icons.settings),
            onPressed: () {},
          ),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: () async {
          ref.invalidate(homeDataProvider);
        },
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            _buildSectionTitle(context, "Today's Reminders"),
            SizedBox(
              height: 60,
              child: recentNotes.isEmpty
                  ? _buildEmptyState(context, 'No reminders today')
                  : ListView.builder(
                      scrollDirection: Axis.horizontal,
                      itemCount: 0,
                      itemBuilder: (context, index) => Container(),
                    ),
            ),
            const SizedBox(height: 24),
            _buildSectionTitle(context, "Upcoming"),
            SizedBox(
              height: 60,
              child: ListView.builder(
                scrollDirection: Axis.horizontal,
                itemCount: 0,
                itemBuilder: (context, index) => Container(),
              ),
            ),
            const SizedBox(height: 24),
            _buildSectionTitle(context, "Recent Notes"),
            recentNotes.isEmpty
                ? _buildEmptyState(context, 'No recent notes')
                : ListView.builder(
                    shrinkWrap: true,
                    physics: const NeverScrollableScrollPhysics(),
                    itemCount: recentNotes.length,
                    itemBuilder: (context, index) {
                      final note = recentNotes[index];
                      return _buildNoteCard(context, note, _goToNotes);
                    },
                  ),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () {
          Navigator.pushNamed(context, '/note-editor');
        },
        child: const Icon(Icons.add),
      ),
    );
  }

  Widget _buildSectionTitle(BuildContext context, String title) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Text(
        title,
        style: Theme.of(context).textTheme.titleLarge?.copyWith(
              fontWeight: FontWeight.bold,
              color: AppColors.textPrimary,
            ),
      ),
    );
  }

  Widget _buildNoteCard(BuildContext context, Note note, VoidCallback onTap) {
    return Card(
      margin: const EdgeInsets.only(bottom: 8),
      child: ListTile(
        title: Text(
          note.title,
          style: const TextStyle(fontWeight: FontWeight.bold),
          maxLines: 1,
          overflow: TextOverflow.ellipsis,
        ),
        subtitle: Text(
          note.content,
          maxLines: 2,
          overflow: TextOverflow.ellipsis,
        ),
        trailing: note.category != null && note.category!.isNotEmpty
            ? Chip(
                label: Text(note.category!),
                backgroundColor: AppColors.surfaceVariant,
                labelStyle: const TextStyle(fontSize: 12),
              )
            : null,
        onTap: onTap,
      ),
    );
  }

  Widget _buildEmptyState(BuildContext context, String message) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          children: [
            Icon(
              Icons.note,
              size: 48,
              color: AppColors.textMuted,
            ),
            const SizedBox(height: 16),
            Text(
              message,
              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                    color: AppColors.textMuted,
                  ),
            ),
          ],
        ),
      ),
    );
  }
}
