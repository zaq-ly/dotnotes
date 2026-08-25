import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:googleapis/calendar/v3.dart' as calendar;
import 'google_auth_service.dart';

class GoogleCalendarService {
  final GoogleAuthService _authService;

  GoogleCalendarService(this._authService);

  Future<String?> createOrUpdateEvent({
    required String? eventId,
    required String title,
    required String description,
    required DateTime reminderTime,
  }) async {
    try {
      final client = await _authService.getAuthenticatedClient();
      if (client == null) return null;

      final calendarApi = calendar.CalendarApi(client);
      final utcStart = reminderTime.toUtc();
      final utcEnd = reminderTime.add(const Duration(minutes: 30)).toUtc();

      final event = calendar.Event(
        summary: title.isNotEmpty ? title : '.notes Reminder',
        description: description,
        start: calendar.EventDateTime(dateTime: utcStart),
        end: calendar.EventDateTime(dateTime: utcEnd),
        reminders: calendar.EventReminders(
          useDefault: false,
          overrides: [
            calendar.EventReminder(method: 'email', minutes: 0),
            calendar.EventReminder(method: 'popup', minutes: 0),
          ],
        ),
      );

      if (eventId != null && eventId.isNotEmpty) {
        try {
          final updated =
              await calendarApi.events.update(event, 'primary', eventId);
          return updated.id;
        } catch (_) {
          // If event was deleted remotely, insert as new
          final inserted = await calendarApi.events.insert(event, 'primary');
          return inserted.id;
        }
      } else {
        final inserted = await calendarApi.events.insert(event, 'primary');
        return inserted.id;
      }
    } catch (e) {
      debugPrint('[GoogleCalendarService] Failed to create/update event: $e');
      return null;
    }
  }

  Future<void> deleteEvent(String eventId) async {
    if (eventId.isEmpty) return;
    try {
      final client = await _authService.getAuthenticatedClient();
      if (client == null) return;

      final calendarApi = calendar.CalendarApi(client);
      await calendarApi.events.delete('primary', eventId);
    } catch (e) {
      debugPrint('[GoogleCalendarService] Failed to delete event: $e');
    }
  }
}

final googleCalendarServiceProvider = Provider<GoogleCalendarService>((ref) {
  final authService = ref.watch(googleAuthServiceProvider);
  return GoogleCalendarService(authService);
});
