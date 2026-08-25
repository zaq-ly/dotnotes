import 'package:flutter_test/flutter_test.dart';
import 'package:dotnotes/features/reminder/alarm_service.dart';

void main() {
  test('AlarmService produces stable 32-bit integer alarm ID from noteId', () {
    final service = AlarmService();

    final id1 = service.noteIdToAlarmId('alarm-test-uuid-1');
    final id2 = service.noteIdToAlarmId('alarm-test-uuid-1');
    final id3 = service.noteIdToAlarmId('alarm-test-uuid-2');

    expect(id1, id2);
    expect(id1 >= 0, true);
    expect(id1 <= 2147483647, true);
    expect(id1 != id3, true);
  });
}
