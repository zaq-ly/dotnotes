// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for English (`en`).
class AppLocalizationsEn extends AppLocalizations {
  AppLocalizationsEn([String locale = 'en']) : super(locale);

  @override
  String get appName => '.notes';

  @override
  String get notesTitle => 'Notes';

  @override
  String get newNote => 'New Note';

  @override
  String get editNote => 'Edit Note';

  @override
  String get titlePlaceholder => 'Title';

  @override
  String get descriptionPlaceholder => 'Start typing your note...';

  @override
  String get save => 'Save';

  @override
  String get delete => 'Delete';

  @override
  String get cancel => 'Cancel';

  @override
  String get done => 'Done';

  @override
  String get settings => 'Settings';

  @override
  String get signInWithGoogle => 'Sign in with Google';

  @override
  String get signOut => 'Sign Out';

  @override
  String get theme => 'Theme';

  @override
  String get themeSystem => 'System';

  @override
  String get themeLight => 'Light';

  @override
  String get themeDark => 'Dark';

  @override
  String get language => 'Language';

  @override
  String get languageEn => 'English';

  @override
  String get languageId => 'Indonesian';

  @override
  String get syncNow => 'Sync Now';

  @override
  String get syncing => 'Syncing...';

  @override
  String get lastSynced => 'Last synced';

  @override
  String get reminder => 'Reminder';

  @override
  String get setReminder => 'Set Reminder';

  @override
  String get noReminder => 'No Reminder';

  @override
  String get removeReminder => 'Remove Reminder';

  @override
  String get selectDate => 'Select Date';

  @override
  String get selectTime => 'Select Time';

  @override
  String get priority => 'Priority Level';

  @override
  String get reminderNormal => 'Standard Notification';

  @override
  String get reminderEmail => 'Notification + Email';

  @override
  String get reminderAlarm => 'Notification + Email + Alarm';

  @override
  String get dismiss => 'Dismiss';

  @override
  String get snooze => 'Snooze';

  @override
  String get alarmRinging => 'Reminder Alarm';

  @override
  String get emptyNotes => 'No notes yet. Tap + to add one.';

  @override
  String get searchPlaceholder => 'Search notes...';
}
