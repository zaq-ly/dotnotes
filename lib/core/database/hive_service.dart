import 'package:hive_flutter/hive_flutter.dart';
import 'package:dotnotes/core/database/boxes.dart';

class HiveService {
  static late Box notesBox;
  static late Box remindersBox;
  static late Box categoriesBox;

  Future<void> init() async {
    await Hive.initFlutter();
    Hive.registerAdapter(NoteAdapter());
    Hive.registerAdapter(ReminderAdapter());
    Hive.registerAdapter(CategoryAdapter());
    
    notesBox = await Hive.openBox(notesBox);
    remindersBox = await Hive.openBox(remindersBox);
    categoriesBox = await Hive.openBox(categoriesBox);
  }

  void closeBoxes() {
    Hive.close();
  }
}
