import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../../../core/constants/app_constants.dart';

final sharedPreferencesProvider = Provider<SharedPreferences>((ref) {
  throw UnimplementedError('Initialize sharedPreferencesProvider in main.dart');
});

class SettingsState {
  final ThemeMode themeMode;
  final Locale? locale;

  const SettingsState({
    this.themeMode = ThemeMode.system,
    this.locale,
  });

  SettingsState copyWith({
    ThemeMode? themeMode,
    Locale? Function()? locale,
  }) {
    return SettingsState(
      themeMode: themeMode ?? this.themeMode,
      locale: locale != null ? locale() : this.locale,
    );
  }
}

class SettingsNotifier extends StateNotifier<SettingsState> {
  final SharedPreferences _prefs;

  SettingsNotifier(this._prefs) : super(const SettingsState()) {
    _loadSettings();
  }

  void _loadSettings() {
    final themeIndex = _prefs.getInt(AppConstants.prefThemeModeKey);
    final themeMode = themeIndex != null && themeIndex < ThemeMode.values.length
        ? ThemeMode.values[themeIndex]
        : ThemeMode.system;

    final languageCode = _prefs.getString(AppConstants.prefLocaleKey);
    final locale = languageCode != null ? Locale(languageCode) : null;

    state = SettingsState(themeMode: themeMode, locale: locale);
  }

  Future<void> setThemeMode(ThemeMode mode) async {
    await _prefs.setInt(AppConstants.prefThemeModeKey, mode.index);
    state = state.copyWith(themeMode: mode);
  }

  Future<void> setLocale(Locale? locale) async {
    if (locale == null) {
      await _prefs.remove(AppConstants.prefLocaleKey);
      state = state.copyWith(locale: () => null);
    } else {
      await _prefs.setString(AppConstants.prefLocaleKey, locale.languageCode);
      state = state.copyWith(locale: () => locale);
    }
  }
}

final settingsNotifierProvider =
    StateNotifierProvider<SettingsNotifier, SettingsState>((ref) {
  final prefs = ref.watch(sharedPreferencesProvider);
  return SettingsNotifier(prefs);
});
