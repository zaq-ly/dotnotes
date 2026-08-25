import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';
import 'package:package_info_plus/package_info_plus.dart';
import '../../../l10n/app_localizations.dart';
import '../../auth/application/auth_notifier.dart';
import '../../sync/application/sync_notifier.dart';
import '../application/settings_notifier.dart';

class SettingsScreen extends ConsumerWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l10n = AppLocalizations.of(context)!;
    final theme = Theme.of(context);
    final settings = ref.watch(settingsNotifierProvider);
    final notifier = ref.read(settingsNotifierProvider.notifier);
    final user = ref.watch(authNotifierProvider);
    final authNotifier = ref.read(authNotifierProvider.notifier);
    final syncState = ref.watch(syncNotifierProvider);
    final syncNotifier = ref.read(syncNotifierProvider.notifier);

    return Scaffold(
      appBar: AppBar(
        title: Text(l10n.settings),
      ),
      body: ListView(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
        children: [
          // Google Account Section
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16.0),
              child: user != null
                  ? Row(
                      children: [
                        CircleAvatar(
                          backgroundImage: user.photoUrl != null
                              ? NetworkImage(user.photoUrl!)
                              : null,
                          child: user.photoUrl == null
                              ? Text(user.displayName?.substring(0, 1) ?? 'U')
                              : null,
                        ),
                        const SizedBox(width: 14),
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                user.displayName ?? 'Google User',
                                style: theme.textTheme.titleMedium?.copyWith(
                                  fontWeight: FontWeight.w600,
                                ),
                              ),
                              Text(
                                user.email,
                                style: theme.textTheme.bodySmall,
                                overflow: TextOverflow.ellipsis,
                              ),
                            ],
                          ),
                        ),
                        OutlinedButton(
                          onPressed: () => authNotifier.signOut(),
                          child: Text(l10n.signOut),
                        ),
                      ],
                    )
                  : Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'Google Account',
                          style: theme.textTheme.titleMedium?.copyWith(
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                        const SizedBox(height: 6),
                        Text(
                          'Sign in to enable automatic email reminders and multi-device cloud sync.',
                          style: theme.textTheme.bodySmall,
                        ),
                        const SizedBox(height: 14),
                        SizedBox(
                          width: double.infinity,
                          child: FilledButton.icon(
                            icon: const Icon(Icons.login),
                            label: Text(l10n.signInWithGoogle),
                            onPressed: () => authNotifier.signIn(),
                          ),
                        ),
                      ],
                    ),
            ),
          ),
          const SizedBox(height: 12),

          // Cloud Sync Section (Google Drive)
          if (user != null) ...[
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Google Drive Sync',
                      style: theme.textTheme.titleMedium?.copyWith(
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                    const SizedBox(height: 6),
                    Text(
                      syncState.isSyncing
                          ? l10n.syncing
                          : syncState.lastSynced != null
                              ? '${l10n.lastSynced}: ${DateFormat('dd MMM yyyy, HH:mm').format(syncState.lastSynced!)}'
                              : 'Not synced yet',
                      style: theme.textTheme.bodySmall?.copyWith(
                        color: theme.textTheme.bodySmall?.color?.withAlpha(180),
                      ),
                    ),
                    const SizedBox(height: 14),
                    SizedBox(
                      width: double.infinity,
                      child: FilledButton.icon(
                        icon: syncState.isSyncing
                            ? const SizedBox(
                                width: 18,
                                height: 18,
                                child: CircularProgressIndicator(
                                  strokeWidth: 2,
                                  color: Colors.white,
                                ),
                              )
                            : const Icon(Icons.sync),
                        label: Text(
                          syncState.isSyncing ? l10n.syncing : l10n.syncNow,
                        ),
                        onPressed:
                            syncState.isSyncing ? null : () => syncNotifier.sync(),
                      ),
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 12),
          ],

          // Theme Section
          Card(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Padding(
                  padding: const EdgeInsets.fromLTRB(16, 16, 16, 8),
                  child: Text(
                    l10n.theme,
                    style: theme.textTheme.titleMedium?.copyWith(
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ),
                RadioGroup<ThemeMode>(
                  groupValue: settings.themeMode,
                  onChanged: (mode) {
                    if (mode != null) notifier.setThemeMode(mode);
                  },
                  child: Column(
                    children: [
                      RadioListTile<ThemeMode>(
                        title: Text(l10n.themeSystem),
                        value: ThemeMode.system,
                      ),
                      RadioListTile<ThemeMode>(
                        title: Text(l10n.themeLight),
                        value: ThemeMode.light,
                      ),
                      RadioListTile<ThemeMode>(
                        title: Text(l10n.themeDark),
                        value: ThemeMode.dark,
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 12),

          // Language Section
          Card(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Padding(
                  padding: const EdgeInsets.fromLTRB(16, 16, 16, 8),
                  child: Text(
                    l10n.language,
                    style: theme.textTheme.titleMedium?.copyWith(
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ),
                RadioGroup<String>(
                  groupValue: settings.locale?.languageCode ?? 'en',
                  onChanged: (code) {
                    if (code != null) notifier.setLocale(Locale(code));
                  },
                  child: Column(
                    children: [
                      RadioListTile<String>(
                        title: Text(l10n.languageEn),
                        value: 'en',
                      ),
                      RadioListTile<String>(
                        title: Text(l10n.languageId),
                        value: 'id',
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 24),

          // Version info
          FutureBuilder<PackageInfo>(
            future: PackageInfo.fromPlatform(),
            builder: (context, snapshot) {
              final version = snapshot.hasData
                  ? 'v${snapshot.data!.version}+${snapshot.data!.buildNumber}'
                  : '...';
              return Center(
                child: Text(
                  '${l10n.appName} $version',
                  style: theme.textTheme.bodySmall,
                ),
              );
            },
          ),
          const SizedBox(height: 16),
        ],
      ),
    );
  }
}
