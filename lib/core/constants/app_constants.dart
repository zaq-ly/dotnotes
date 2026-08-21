class AppConstants {
  AppConstants._();

  static const String appName = '.notes';
  static const String supabaseUrl = String.fromEnvironment('SUPABASE_URL');
  static const String supabaseAnonKey = String.fromEnvironment('SUPABASE_ANON_KEY');
  
  static const int notificationIdOffset = 1000;
  static const int alarmIdOffset = 5000;
  
  static const String notesTableName = 'notes';
  static const String localDbName = 'notes.db';
  
  static const Duration syncInterval = Duration(minutes: 5);
  static const Duration reminderCheckInterval = Duration(minutes: 1);
}

class AppSpacing {
  AppSpacing._();

  static const double xs = 4.0;
  static const double sm = 8.0;
  static const double md = 12.0;
  static const double lg = 16.0;
  static const double xl = 24.0;
  static const double xxl = 32.0;
  static const double xxxl = 48.0;
  
  static const double cardPadding = 16.0;
  static const double pagePadding = 16.0;
  static const double cardSpacing = 8.0;
  static const double sectionSpacing = 24.0;
}

class AppRadius {
  AppRadius._();

  static const double card = 12.0;
  static const double button = 8.0;
  static const double input = 8.0;
  static const double fab = 16.0;
}