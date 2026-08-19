import 'package:flutter/material.dart';
import 'package:dotnotes/features/home/screens/home_screen.dart';
import 'package:dotnotes/features/notes/screens/notes_screen.dart';
import 'package:dotnotes/features/notes/screens/note_editor_screen.dart';
import 'package:dotnotes/features/reminders/screens/reminder_editor_screen.dart';

class AppRoutes {
  static const String home = '/';
  static const String notes = '/notes';
  static const String noteEditor = '/note-editor';
  static const String reminderEditor = '/reminder-editor';

  static Route<dynamic> generateRoute(RouteSettings settings) {
    switch (settings.name) {
      case home:
        return MaterialPageRoute(builder: (_) => const HomeScreen());
      case notes:
        return MaterialPageRoute(builder: (_) => const NotesScreen());
      case noteEditor:
        final args = settings.arguments as Map<String, dynamic>?;
        return MaterialPageRoute(
          builder: (_) => NoteEditorScreen(
            noteId: args?['noteId'] as String?,
          ),
        );
      case reminderEditor:
        final args = settings.arguments as Map<String, dynamic>;
        return MaterialPageRoute(
          builder: (_) => ReminderEditorScreen(
            noteId: args['noteId'] as String,
            reminderId: args['reminderId'] as String?,
          ),
        );
      default:
        return MaterialPageRoute(
          builder: (_) => Scaffold(
            body: Center(
              child: Text('Route ${settings.name} not found'),
            ),
          ),
        );
    }
  }
}
