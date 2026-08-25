import 'package:flutter_test/flutter_test.dart';
import 'package:googleapis_auth/googleapis_auth.dart';
import 'package:dotnotes/data/remote/google/google_auth_service.dart';
import 'package:dotnotes/data/remote/google/google_drive_sync_service.dart';
import 'package:dotnotes/domain/entities/note.dart';

class MockGoogleAuthService extends GoogleAuthService {
  @override
  Future<AuthClient?> getAuthenticatedClient() async => null;
}

void main() {
  group('GoogleDriveSyncService merge logic (Last-Write-Wins)', () {
    test('Correctly merges local and remote notes based on updatedAt timestamp', () {
      final service = GoogleDriveSyncService(MockGoogleAuthService());

      final t1 = DateTime(2026, 8, 20, 10, 0);
      final t2 = DateTime(2026, 8, 21, 10, 0);
      final t3 = DateTime(2026, 8, 22, 10, 0);

      // Note 1: Local is newer than remote
      final localNote1 = NoteEntity(
        id: 'note-1',
        title: 'Note 1 Local Updated',
        description: 'Local desc',
        isSynced: false,
        createdAt: t1,
        updatedAt: t3,
      );
      final remoteNote1 = NoteEntity(
        id: 'note-1',
        title: 'Note 1 Remote Old',
        description: 'Remote desc',
        isSynced: true,
        createdAt: t1,
        updatedAt: t2,
      );

      // Note 2: Remote is newer than local
      final localNote2 = NoteEntity(
        id: 'note-2',
        title: 'Note 2 Local Old',
        description: 'Local desc',
        isSynced: true,
        createdAt: t1,
        updatedAt: t1,
      );
      final remoteNote2 = NoteEntity(
        id: 'note-2',
        title: 'Note 2 Remote Updated',
        description: 'Remote desc',
        isSynced: true,
        createdAt: t1,
        updatedAt: t3,
      );

      // Note 3: Only in remote (downloaded from another device)
      final remoteNote3 = NoteEntity(
        id: 'note-3',
        title: 'Note 3 Remote Only',
        description: 'From device 2',
        isSynced: true,
        createdAt: t2,
        updatedAt: t2,
      );

      final merged = service.mergeNotes(
        [localNote1, localNote2],
        [remoteNote1, remoteNote2, remoteNote3],
      );

      expect(merged.length, 3);

      final n1 = merged.firstWhere((n) => n.id == 'note-1');
      expect(n1.title, 'Note 1 Local Updated');

      final n2 = merged.firstWhere((n) => n.id == 'note-2');
      expect(n2.title, 'Note 2 Remote Updated');

      final n3 = merged.firstWhere((n) => n.id == 'note-3');
      expect(n3.title, 'Note 3 Remote Only');
    });
  });
}
