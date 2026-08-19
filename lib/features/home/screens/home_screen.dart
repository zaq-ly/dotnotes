import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';
import 'package:dotnotes/app/routes.dart';
import 'package:dotnotes/app/theme.dart';
import 'package:dotnotes/features/home/providers/home_provider.dart';
import 'package:dotnotes/features/notes/models/note.dart';
import 'package:dotnotes/features/notes/providers/notes_provider.dart';
import 'package:dotnotes/features/notes/widgets/note_card.dart';
import 'package:dotnotes/features/reminders/models/reminder.dart';
import 'package:dotnotes/shared/widgets/brand.dart';
import 'package:dotnotes/shared/widgets/paper_card.dart';

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
        title: const Wordmark(),
        actions: [
          IconButton(
            tooltip: 'Search notes',
            icon: const Icon(Icons.search),
            onPressed: () {
              Navigator.pushNamed(context, AppRoutes.notes);
            },
          ),
          IconButton(
            tooltip: 'All notes',
            icon: const Icon(Icons.note_alt_outlined),
            onPressed: () {
              Navigator.pushNamed(context, AppRoutes.notes);
            },
          ),
        ],
      ),
      body: RefreshIndicator(
        color: AppColors.signal,
        backgroundColor: AppColors.raise,
        onRefresh: () async {
          ref.invalidate(homeDataProvider);
        },
        child: ListView(
          padding: const EdgeInsets.fromLTRB(16, 4, 16, 96),
          children: [
            _buildSection(
              context,
              ref,
              label: "Today's reminders",
              count: todayReminders.length,
              active: todayReminders.isNotEmpty,
              child: todayReminders.isEmpty
                  ? const _EmptyLine(message: 'Nothing armed for today')
                  : SizedBox(
                      height: 120,
                      child: ListView.builder(
                        scrollDirection: Axis.horizontal,
                        itemCount: todayReminders.length,
                        itemBuilder: (context, index) {
                          final reminder = todayReminders[index];
                          return _buildReminderCard(context, ref, reminder);
                        },
                      ),
                    ),
            ),
            _buildSection(
              context,
              ref,
              label: 'Upcoming',
              count: upcomingReminders.length,
              active: upcomingReminders.isNotEmpty,
              child: upcomingReminders.isEmpty
                  ? const _EmptyLine(message: 'Nothing scheduled ahead')
                  : SizedBox(
                      height: 120,
                      child: ListView.builder(
                        scrollDirection: Axis.horizontal,
                        itemCount: upcomingReminders.length,
                        itemBuilder: (context, index) {
                          final reminder = upcomingReminders[index];
                          return _buildReminderCard(context, ref, reminder);
                        },
                      ),
                    ),
            ),
            _buildSection(
              context,
              ref,
              label: 'Recent notes',
              count: recentNotes.length,
              active: recentNotes.isNotEmpty,
              child: recentNotes.isEmpty
                  ? const _EmptyLine(message: 'No notes yet')
                  : Column(
                      children: recentNotes.map((note) {
                        return NoteCard(
                          note: note,
                          onTap: () {
                            Navigator.pushNamed(
                              context,
                              AppRoutes.noteEditor,
                              arguments: {'noteId': note.id},
                            );
                          },
                        );
                      }).toList(),
                    ),
            ),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        tooltip: 'New note',
        onPressed: () {
          Navigator.pushNamed(context, AppRoutes.noteEditor);
        },
        child: const Icon(Icons.add),
      ),
    );
  }

  Widget _buildSection(
    BuildContext context,
    WidgetRef ref, {
    required String label,
    required int count,
    required bool active,
    required Widget child,
  }) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 28),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Padding(
            padding: const EdgeInsets.only(bottom: 12, left: 2),
            child: SectionLabel(text: label, count: count, active: active),
          ),
          child,
        ],
      ),
    );
  }

  Widget _buildReminderCard(
    BuildContext context,
    WidgetRef ref,
    Reminder reminder,
  ) {
    final notesAsync = ref.watch(notesProvider);
    final notes = notesAsync.valueOrNull ?? [];
    final note = notes.cast<Note?>().firstWhere(
          (n) => n?.id == reminder.noteId,
          orElse: () => null,
        );

    final timeStr = DateFormat('MMM d · HH:mm').format(reminder.scheduledAt);
    final isToday = _isSameDay(reminder.scheduledAt, DateTime.now());
    final armed = reminder.alarmEnabled;
    final live = armed && isToday;

    return Container(
      width: 210,
      margin: const EdgeInsets.only(right: 12),
      child: PaperCard(
        padding: const EdgeInsets.all(14),
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
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Expanded(
                  child: Text(
                    note?.title.isNotEmpty == true ? note!.title : 'Reminder',
                    style: const TextStyle(
                      fontFamily: AppFonts.body,
                      fontWeight: FontWeight.w700,
                      fontSize: 15,
                      color: AppColors.ink,
                    ),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                  ),
                ),
                const SizedBox(width: 8),
                _StatusDot(live: live, armed: armed),
              ],
            ),
            const Spacer(),
            Row(
              children: [
                Icon(
                  Icons.alarm,
                  size: 13,
                  color: armed ? AppColors.signal : AppColors.inkSoft,
                ),
                const SizedBox(width: 5),
                Expanded(
                  child: Text(
                    timeStr,
                    style: const TextStyle(
                      fontFamily: AppFonts.mono,
                      fontSize: 11,
                      fontWeight: FontWeight.w700,
                      color: AppColors.inkSoft,
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
    );
  }

  bool _isSameDay(DateTime a, DateTime b) {
    return a.year == b.year && a.month == b.month && a.day == b.day;
  }
}

class _EmptyLine extends StatelessWidget {
  final String message;

  const _EmptyLine({required this.message});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 10),
      child: Row(
        children: [
          Container(
            width: 8,
            height: 8,
            decoration: const BoxDecoration(
              shape: BoxShape.circle,
              color: AppColors.line,
            ),
          ),
          const SizedBox(width: 10),
          Text(
            message,
            style: const TextStyle(
              fontFamily: AppFonts.mono,
              fontSize: 12,
              color: AppColors.muted,
            ),
          ),
        ],
      ),
    );
  }
}

class _StatusDot extends StatefulWidget {
  final bool live;
  final bool armed;

  const _StatusDot({required this.live, required this.armed});

  @override
  State<_StatusDot> createState() => _StatusDotState();
}

class _StatusDotState extends State<_StatusDot>
    with SingleTickerProviderStateMixin {
  late final AnimationController _controller;
  late final Animation<double> _pulse;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 1400),
    );
    _pulse = TweenSequence<double>([
      TweenSequenceItem(tween: Tween(begin: 1.0, end: 1.35), weight: 1),
      TweenSequenceItem(tween: Tween(begin: 1.35, end: 1.0), weight: 1),
    ]).animate(
      CurvedAnimation(parent: _controller, curve: Curves.easeInOut),
    );
    if (widget.live) {
      _controller.repeat();
    }
  }

  @override
  void didUpdateWidget(covariant _StatusDot oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.live && !_controller.isAnimating) {
      _controller.repeat();
    } else if (!widget.live && _controller.isAnimating) {
      _controller.stop();
      _controller.value = 1.0;
    }
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final reduced = MediaQuery.disableAnimationsOf(context);
    final color = widget.armed ? AppColors.signal : AppColors.inkSoft;

    return Container(
      width: 10,
      height: 10,
      decoration: BoxDecoration(
        shape: BoxShape.circle,
        color: color.withValues(alpha: 0.22),
      ),
      alignment: Alignment.center,
      child: AnimatedBuilder(
        animation: _pulse,
        builder: (context, _) {
          final scale = reduced || !widget.live ? 1.0 : _pulse.value;
          return Transform.scale(
            scale: scale,
            child: Container(
              width: 6,
              height: 6,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                color: color,
              ),
            ),
          );
        },
      ),
    );
  }
}