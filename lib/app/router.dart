import 'package:flutter/material.dart';
import '../domain/entities/note.dart';
import '../features/note_editor/presentation/note_editor_screen.dart';
import '../features/notes_list/presentation/notes_list_screen.dart';
import '../features/settings/presentation/settings_screen.dart';

abstract class AppRoutes {
  static const home = '/';
  static const settings = '/settings';
  static const editor = '/editor';

  static Route<dynamic>? onGenerateRoute(RouteSettings routeSettings) {
    switch (routeSettings.name) {
      case home:
        return MaterialPageRoute(builder: (_) => const NotesListScreen());
      case settings:
        return MaterialPageRoute(builder: (_) => const SettingsScreen());
      case editor:
        final note = routeSettings.arguments as NoteEntity?;
        return MaterialPageRoute(
          builder: (_) => NoteEditorScreen(initialNote: note),
        );
      default:
        return MaterialPageRoute(builder: (_) => const NotesListScreen());
    }
  }
}
