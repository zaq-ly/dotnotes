import 'package:flutter/material.dart';
import '../features/notes_list/presentation/notes_list_screen.dart';
import '../features/settings/presentation/settings_screen.dart';

abstract class AppRoutes {
  static const home = '/';
  static const settings = '/settings';

  static Route<dynamic>? onGenerateRoute(RouteSettings routeSettings) {
    switch (routeSettings.name) {
      case home:
        return MaterialPageRoute(builder: (_) => const NotesListScreen());
      case settings:
        return MaterialPageRoute(builder: (_) => const SettingsScreen());
      default:
        return MaterialPageRoute(builder: (_) => const NotesListScreen());
    }
  }
}
