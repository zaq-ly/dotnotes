import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../app/theme/app_colors.dart';
import '../../../l10n/app_localizations.dart';
import '../alarm_service.dart';
import '../notification_service.dart';

class AlarmArguments {
  final String noteId;
  final String title;
  final String description;

  const AlarmArguments({
    required this.noteId,
    required this.title,
    required this.description,
  });
}

class AlarmRingingScreen extends ConsumerStatefulWidget {
  final AlarmArguments arguments;

  const AlarmRingingScreen({super.key, required this.arguments});

  @override
  ConsumerState<AlarmRingingScreen> createState() => _AlarmRingingScreenState();
}

class _AlarmRingingScreenState extends ConsumerState<AlarmRingingScreen>
    with SingleTickerProviderStateMixin {
  late AnimationController _animController;
  late Animation<double> _scaleAnimation;

  @override
  void initState() {
    super.initState();
    _animController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 900),
    )..repeat(reverse: true);

    _scaleAnimation = Tween<double>(begin: 0.9, end: 1.15).animate(
      CurvedAnimation(parent: _animController, curve: Curves.easeInOut),
    );

    // Provide haptic feedback pattern on alarm trigger
    HapticFeedback.vibrate();
  }

  @override
  void dispose() {
    _animController.dispose();
    super.dispose();
  }

  Future<void> _handleSnooze(BuildContext context) async {
    final snoozeTime = DateTime.now().add(const Duration(minutes: 5));
    final notifService = ref.read(notificationServiceProvider);
    final alarmService = ref.read(alarmServiceProvider);

    await notifService.scheduleNoteNotification(
      noteId: widget.arguments.noteId,
      title: widget.arguments.title,
      body: widget.arguments.description,
      scheduledDate: snoozeTime,
    );

    await alarmService.scheduleAlarm(
      noteId: widget.arguments.noteId,
      scheduledDate: snoozeTime,
      title: widget.arguments.title,
      description: widget.arguments.description,
    );

    if (context.mounted) {
      Navigator.of(context).pop();
    }
  }

  void _handleDismiss(BuildContext context) {
    Navigator.of(context).pop();
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    final theme = Theme.of(context);

    return Scaffold(
      backgroundColor: theme.scaffoldBackgroundColor,
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 32),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              // Header & Alarm Icon
              Column(
                children: [
                  const SizedBox(height: 32),
                  ScaleTransition(
                    scale: _scaleAnimation,
                    child: Container(
                      padding: const EdgeInsets.all(24),
                      decoration: BoxDecoration(
                        color: AppColors.priorityAlarm.withAlpha(35),
                        shape: BoxShape.circle,
                        border: Border.all(
                          color: AppColors.priorityAlarm.withAlpha(120),
                          width: 2,
                        ),
                      ),
                      child: const Icon(
                        Icons.alarm_on,
                        size: 64,
                        color: AppColors.priorityAlarm,
                      ),
                    ),
                  ),
                  const SizedBox(height: 24),
                  Text(
                    l10n.alarmRinging,
                    style: theme.textTheme.bodySmall?.copyWith(
                      color: AppColors.priorityAlarm,
                      fontWeight: FontWeight.w600,
                      letterSpacing: 1.2,
                    ),
                  ),
                ],
              ),

              // Note Content Card
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(20.0),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        widget.arguments.title.isNotEmpty
                            ? widget.arguments.title
                            : 'Untitled',
                        style: theme.textTheme.headlineMedium?.copyWith(
                          fontSize: 20,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                      if (widget.arguments.description.isNotEmpty) ...[
                        const SizedBox(height: 10),
                        Text(
                          widget.arguments.description,
                          style: theme.textTheme.bodyMedium?.copyWith(
                            fontSize: 15,
                            height: 1.5,
                          ),
                          maxLines: 8,
                          overflow: TextOverflow.ellipsis,
                        ),
                      ],
                    ],
                  ),
                ),
              ),

              // Action Buttons
              Column(
                children: [
                  Row(
                    children: [
                      Expanded(
                        child: OutlinedButton.icon(
                          icon: const Icon(Icons.snooze),
                          label: Text('${l10n.snooze} (+5m)'),
                          style: OutlinedButton.styleFrom(
                            padding: const EdgeInsets.symmetric(vertical: 16),
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(12),
                            ),
                          ),
                          onPressed: () => _handleSnooze(context),
                        ),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: FilledButton.icon(
                          icon: const Icon(Icons.close),
                          label: Text(l10n.dismiss),
                          style: FilledButton.styleFrom(
                            backgroundColor: AppColors.priorityAlarm,
                            padding: const EdgeInsets.symmetric(vertical: 16),
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(12),
                            ),
                          ),
                          onPressed: () => _handleDismiss(context),
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}
