import 'package:drift/drift.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../../../data/local/drift/database.dart';
import '../../../data/models/note_model.dart';
import '../../../data/remote/google/google_drive_sync_service.dart';
import '../../../features/settings/application/settings_notifier.dart';

class SyncState {
  final bool isSyncing;
  final DateTime? lastSynced;
  final String? error;

  const SyncState({
    this.isSyncing = false,
    this.lastSynced,
    this.error,
  });

  SyncState copyWith({
    bool? isSyncing,
    DateTime? lastSynced,
    String? Function()? error,
  }) {
    return SyncState(
      isSyncing: isSyncing ?? this.isSyncing,
      lastSynced: lastSynced ?? this.lastSynced,
      error: error != null ? error() : this.error,
    );
  }
}

class SyncNotifier extends StateNotifier<SyncState> {
  final GoogleDriveSyncService _syncService;
  final AppDatabase _database;
  final SharedPreferences _prefs;

  static const _lastSyncedKey = 'dotnotes_last_synced_timestamp';

  SyncNotifier({
    required GoogleDriveSyncService syncServiceUseCase,
    required AppDatabase databaseInstance,
    required SharedPreferences preferences,
  })  : _syncService = syncServiceUseCase,
        _database = databaseInstance,
        _prefs = preferences,
        super(const SyncState()) {
    _loadLastSynced();
  }

  void _loadLastSynced() {
    final str = _prefs.getString(_lastSyncedKey);
    if (str != null) {
      state = state.copyWith(lastSynced: DateTime.tryParse(str));
    }
  }

  Future<bool> sync() async {
    if (state.isSyncing) return false;

    state = state.copyWith(isSyncing: true, error: () => null);

    try {
      // 1. Get all local notes from SQLite
      final rows = await (_database.select(_database.notesTable)).get();
      final localNotes = rows.map((r) => NoteModel.fromDrift(r)).toList();

      // 2. Perform Drive Sync & Merge
      final mergedNotes = await _syncService.syncNotes(localNotes: localNotes);

      if (mergedNotes != null) {
        // 3. Upsert merged notes back into local SQLite
        await _database.batch((batch) {
          for (final note in mergedNotes) {
            batch.insert(
              _database.notesTable,
              NoteModel.toCompanion(note),
              mode: InsertMode.insertOrReplace,
            );
          }
        });

        final now = DateTime.now();
        await _prefs.setString(_lastSyncedKey, now.toIso8601String());
        state = state.copyWith(isSyncing: false, lastSynced: now);
        return true;
      } else {
        state = state.copyWith(
          isSyncing: false,
          error: () => 'Failed to sync with Google Drive',
        );
        return false;
      }
    } catch (e) {
      state = state.copyWith(
        isSyncing: false,
        error: () => e.toString(),
      );
      return false;
    }
  }
}

final syncNotifierProvider =
    StateNotifierProvider<SyncNotifier, SyncState>((ref) {
  final syncService = ref.watch(googleDriveSyncServiceProvider);
  final database = ref.watch(databaseProvider);
  final prefs = ref.watch(sharedPreferencesProvider);

  return SyncNotifier(
    syncServiceUseCase: syncService,
    databaseInstance: database,
    preferences: prefs,
  );
});
