import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';
import 'package:dotnotes/app/routes.dart';
import 'package:dotnotes/app/theme.dart';
import 'package:dotnotes/features/home/providers/home_provider.dart';
import 'package:dotnotes/features/notes/models/note.dart';
import 'package:dotnotes/features/notes/providers/notes_provider.dart';
import 'package:dotnotes/features/reminders/models/reminder.dart';

class HomeScreen extends ConsumerWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final homeDataAsync = ref.watch(homeDataProvider);

    return homeDataAsync.when(
      data: (homeData) => _buildContent(context, ref, homeData),
      loading: () => const Scaffold(
        body: Center(child: CircularProgressIndicator()),
      ),
      error: (err, stack) => Scaffold(
        body: Center(child: Text('Error: $err')),
      ),
    );
  }

  Widget _buildContent(BuildContext context, WidgetRef ref, HomeData homeData) {
    final recentNotes = homeData.recentNotes;
    final todayReminders = homeData.todayReminders;
    final upcomingReminders = homeData.upcomingReminders;

    return Scaffold(
      appBar: AppBar(
        title: const Text(AppStrings.appName),
        actions: [
          IconButton(
            icon: const Icon(Icons.search),
            onPressed: () {
              Navigator.pushNamed(context, AppRoutes.notes);
            },
          ),
          IconButton(
            icon: const Icon(Icons.note_alt_outlined),
            onPressed: () {
              Navigator.pushNamed(context, AppRoutes.notes);
            },
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
            todayReminders.isEmpty
                ? _buildEmptyState(context, 'No reminders today')
                : SizedBox(
                    height: 100,
                    child: ListView.builder(
                      scrollDirection: Axis.horizontal,
                      itemCount: todayReminders.length,
                      itemBuilder: (context, index) {
                        final reminder = todayReminders[index];
                        return _buildReminderCard(context, ref, reminder);
                      },
                    ),
                  ),
            const SizedBox(height: 24),
            _buildSectionTitle(context, 'Upcoming'),
            upcomingReminders.isEmpty
                ? _buildEmptyState(context, 'No upcoming reminders')
                : SizedBox(
                    height: 100,
                    child: ListView.builder(
                      scrollDirection: Axis.horizontal,
                      itemCount: upcomingReminders.length,
                      itemBuilder: (context, index) {
                        final reminder = upcomingReminders[index];
                        return _buildReminderCard(context, ref, reminder);
                      },
                    ),
                  ),
            const SizedBox(height: 24),
            _buildSectionTitle(context, 'Recent Notes'),
            recentNotes.isEmpty
                ? _buildEmptyState(context, 'No recent notes')
                : ListView.builder(
                    shrinkWrap: true,
                    physics: const NeverScrollableScrollPhysics(),
                    itemCount: recentNotes.length,
                    itemBuilder: (context, index) {
                      final note = recentNotes[index];
                      return _buildNoteCard(context, note, () {
                        Navigator.pushNamed(
                          context,
                          AppRoutes.noteEditor,
                          arguments: {'noteId': note.id},
                        );
                      });
                    },
                  ),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () {
          Navigator.pushNamed(context, AppRoutes.noteEditor);
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

  Widget _buildReminderCard(BuildContext context, WidgetRef ref, Reminder reminder) {
    final notesAsync = ref.watch(notesProvider);
    final notes = notesAsync.valueOrNull ?? [];
    final note = notes.cast<Note?>().firstWhere(
          (n) => n?.id == reminder.noteId,
          orElse: () => null,
        );

    final timeStr = DateFormat('MMM d, HH:mm').format(reminder.scheduledAt);

    return Container(
      width: 200,
      margin: const EdgeInsets.only(right: 12),
      child: Card(
        child: InkWell(
          borderRadius: BorderRadius.circular(12),
          onTap: () {
            Navigator.pushNamed(
              context,
              AppRoutes.reminderEditor,
              arguments: {
                'noteId': reminder.noteId,
                'reminderId': reminder.id,
              },
            );
          },
          child: Padding(
            padding: const EdgeInsets.all(12),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  note?.title.isNotEmpty == true ? note!.title : 'Reminder',
                  style: const TextStyle(
                    fontWeight: FontWeight.bold,
                    fontSize: 14,
                  ),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
                Row(
                  children: [
                    const Icon(
                      Icons.alarm,
                      size: 14,
                      color: AppColors.primary,
                    ),
                    const SizedBox(width: 4),
                    Expanded(
                      child: Text(
                        timeStr,
                        style: const TextStyle(
                          fontSize: 12,
                          color: AppColors.textSecondary,
                        ),
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildNoteCard(BuildContext context, Note note, VoidCallback onTap) {
    return Card(
      margin: const EdgeInsets.only(bottom: 8),
      child: ListTile(
        title: Text(
          note.title.isEmpty ? 'Untitled' : note.title,
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
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            const Icon(
              Icons.note,
              size: 36,
              color: AppColors.textMuted,
            ),
            const SizedBox(height: 8),
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
