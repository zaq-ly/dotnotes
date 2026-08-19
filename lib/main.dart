import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:hive_flutter/hive_flutter.dart';
import 'package:dotnotes/app/app.dart';
import 'package:dotnotes/features/notes/models/note.dart';
import 'package:dotnotes/features/reminders/models/reminder.dart';
import 'package:dotnotes/core/database/boxes.dart' as boxes;
import 'package:dotnotes/core/services/notification_service.dart';
import 'package:dotnotes/core/services/alarm_service.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  
  await Hive.initFlutter();
  
  Hive.registerAdapter(NoteAdapter());
  Hive.registerAdapter(ReminderAdapter());
  Hive.registerAdapter(PriorityAdapter());
  Hive.registerAdapter(RepeatTypeAdapter());
  Hive.registerAdapter(ReminderStatusAdapter());
  
  await Hive.openBox<Note>(boxes.notesBox);
  await Hive.openBox<Reminder>(boxes.remindersBox);
  
  await NotificationService().init();
  await AlarmService().init();
  
  runApp(
    const ProviderScope(
      child: App(),
    ),
  );
}
