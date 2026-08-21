import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'app/app.dart';
import 'data/local/notes_database.dart';
import 'features/reminder/notification_service.dart';
import 'features/reminder/alarm_service.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();

  await NotesDatabase.instance.database;
  await NotificationService().initialize();
  await AlarmService().initialize();

  runApp(const ProviderScope(child: App()));
}