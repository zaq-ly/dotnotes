import 'package:flutter/material.dart';
import 'package:package_info_plus/package_info_plus.dart';
import '../../../core/extensions/extensions.dart';

class SettingsScreen extends StatelessWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Settings')),
      body: ListView(
        children: [
          const SizedBox(height: 16),
          _sectionHeader(context, 'Appearance'),
          ListTile(
            leading: const Icon(Icons.dark_mode_outlined),
            title: const Text('Theme'),
            subtitle: const Text('System default'),
            trailing: const Icon(Icons.chevron_right),
            onTap: () {},
          ),
          const Divider(height: 32),
          _sectionHeader(context, 'Language'),
          ListTile(
            leading: const Icon(Icons.language),
            title: const Text('Language'),
            subtitle: const Text('English'),
            trailing: const Icon(Icons.chevron_right),
            onTap: () {},
          ),
          const Divider(height: 32),
          _sectionHeader(context, 'Account'),
          ListTile(
            leading: const Icon(Icons.person_outline),
            title: const Text('Account'),
            subtitle: const Text('user@example.com'),
            trailing: const Icon(Icons.chevron_right),
            onTap: () {},
          ),
          ListTile(
            leading: const Icon(Icons.logout, color: Colors.red),
            title: const Text('Sign Out', style: TextStyle(color: Colors.red)),
            onTap: () {},
          ),
          const Divider(height: 32),
          _sectionHeader(context, 'About'),
          FutureBuilder<PackageInfo>(
            future: PackageInfo.fromPlatform(),
            builder: (context, snapshot) {
              final version = snapshot.data?.version ?? '-';
              final buildNumber = snapshot.data?.buildNumber ?? '-';
              return ListTile(
                leading: const Icon(Icons.info_outline),
                title: const Text('App Info'),
                subtitle: Text('Version $version ($buildNumber)'),
              );
            },
          ),
        ],
      ),
    );
  }

  Widget _sectionHeader(BuildContext context, String title) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      child: Text(title, style: context.textTheme.titleSmall?.copyWith(color: Theme.of(context).colorScheme.outline)),
    );
  }
}