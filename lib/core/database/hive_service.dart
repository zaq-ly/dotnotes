import 'package:hive_flutter/hive_flutter.dart';
import 'package:dotnotes/core/database/boxes.dart' as boxes;
import 'package:dotnotes/features/notes/models/note.dart';
import 'package:dotnotes/features/reminders/models/reminder.dart';

class HiveService {
  static Future<void> init() async {
    await Hive.initFlutter();
    
    Hive.registerAdapter(NoteAdapter());
    Hive.registerAdapter(ReminderAdapter());
    Hive.registerAdapter(PriorityAdapter());
    Hive.registerAdapter(RepeatTypeAdapter());
    Hive.registerAdapter(ReminderStatusAdapter());
    
    await Hive.openBox<Note>(boxes.notesBox);
    await Hive.openBox<Reminder>(boxes.remindersBox);
  }

  static void closeBoxes() {
    Hive.close();
  }
}
