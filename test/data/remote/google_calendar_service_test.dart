import 'package:flutter_test/flutter_test.dart';
import 'package:googleapis_auth/googleapis_auth.dart';
import 'package:dotnotes/data/remote/google/google_auth_service.dart';
import 'package:dotnotes/data/remote/google/google_calendar_service.dart';

class MockGoogleAuthService extends GoogleAuthService {
  @override
  Future<AuthClient?> getAuthenticatedClient() async => null;
}

void main() {
  test('GoogleCalendarService returns null safely when unauthenticated', () async {
    final mockAuth = MockGoogleAuthService();
    final service = GoogleCalendarService(mockAuth);

    final eventId = await service.createOrUpdateEvent(
      eventId: null,
      title: 'Doctor Appointment',
      description: 'Checkup',
      reminderTime: DateTime.now().add(const Duration(days: 1)),
    );

    expect(eventId, isNull);
  });
}
