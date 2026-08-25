import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:googleapis/drive/v3.dart' as drive;
import '../../models/note_model.dart';
import '../../../domain/entities/note.dart';
import 'google_auth_service.dart';

class GoogleDriveSyncService {
  final GoogleAuthService _authService;
  static const String backupFileName = 'dotnotes_backup.json';

  GoogleDriveSyncService(this._authService);

  List<NoteEntity> mergeNotes(
    List<NoteEntity> localNotes,
    List<NoteEntity> remoteNotes,
  ) {
    final Map<String, NoteEntity> merged = {};

    for (final note in localNotes) {
      merged[note.id] = note;
    }

    for (final remote in remoteNotes) {
      if (merged.containsKey(remote.id)) {
        final local = merged[remote.id]!;
        if (remote.updatedAt.isAfter(local.updatedAt)) {
          merged[remote.id] = remote.copyWith(isSynced: true);
        }
      } else {
        merged[remote.id] = remote.copyWith(isSynced: true);
      }
    }

    return merged.values.toList();
  }

  Future<List<NoteEntity>?> syncNotes({
    required List<NoteEntity> localNotes,
  }) async {
    try {
      final client = await _authService.getAuthenticatedClient();
      if (client == null) {
        debugPrint('[GoogleDriveSyncService] User is not authenticated');
        return null;
      }

      final driveApi = drive.DriveApi(client);

      // 1. Search for backup file in appDataFolder
      final fileList = await driveApi.files.list(
        spaces: 'appDataFolder',
        q: "name = '$backupFileName' and trashed = false",
        $fields: 'files(id, name)',
      );

      String? existingFileId;
      List<NoteEntity> remoteNotes = [];

      if (fileList.files != null && fileList.files!.isNotEmpty) {
        final file = fileList.files!.first;
        existingFileId = file.id;

        // 2. Download and parse remote backup
        try {
          final media = await driveApi.files.get(
            existingFileId!,
            downloadOptions: drive.DownloadOptions.fullMedia,
          ) as drive.Media;

          final bytes = <int>[];
          await for (final chunk in media.stream) {
            bytes.addAll(chunk);
          }

          final jsonString = utf8.decode(bytes);
          final decoded = jsonDecode(jsonString) as Map<String, dynamic>;
          final rawNotes = decoded['notes'] as List<dynamic>? ?? [];

          remoteNotes = rawNotes
              .map((n) => NoteModel.fromJson(n as Map<String, dynamic>))
              .toList();
        } catch (e) {
          debugPrint('[GoogleDriveSyncService] Download/parse error: $e');
        }
      }

      // 3. Bidirectional merge (Last-Write-Wins)
      final mergedNotes = mergeNotes(localNotes, remoteNotes);

      // 4. Serialize and upload merged snapshot to appDataFolder
      final backupPayload = jsonEncode({
        'version': 1,
        'synced_at': DateTime.now().toIso8601String(),
        'notes': mergedNotes.map((n) => NoteModel.toJson(n)).toList(),
      });

      final uploadBytes = utf8.encode(backupPayload);
      final uploadMedia = drive.Media(
        Stream.value(uploadBytes),
        uploadBytes.length,
      );

      if (existingFileId != null) {
        await driveApi.files.update(
          drive.File(),
          existingFileId,
          uploadMedia: uploadMedia,
        );
      } else {
        final newFile = drive.File(
          name: backupFileName,
          parents: ['appDataFolder'],
        );
        await driveApi.files.create(
          newFile,
          uploadMedia: uploadMedia,
        );
      }

      return mergedNotes.map((n) => n.copyWith(isSynced: true)).toList();
    } catch (e) {
      debugPrint('[GoogleDriveSyncService] Sync error: $e');
      return null;
    }
  }
}

final googleDriveSyncServiceProvider = Provider<GoogleDriveSyncService>((ref) {
  final authService = ref.watch(googleAuthServiceProvider);
  return GoogleDriveSyncService(authService);
});
