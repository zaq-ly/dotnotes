import 'package:flutter_test/flutter_test.dart';
import 'package:dotnotes/features/reminder/notification_service.dart';

void main() {
  test('NotificationService generates stable positive 32-bit int IDs from UUID', () {
    final service = NotificationService();

    final id1 = service.noteIdToNotificationId('8b883d90-05c4-4dea-8c5f-dbab4a3397b9');
    final id2 = service.noteIdToNotificationId('8b883d90-05c4-4dea-8c5f-dbab4a3397b9');
    final id3 = service.noteIdToNotificationId('different-note-uuid');

    expect(id1, id2); // Stable for same UUID
    expect(id1 >= 0, true);
    expect(id1 <= 2147483647, true);
    expect(id1 != id3, true);
  });
}
