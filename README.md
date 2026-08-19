# .notes

Aplikasi Android untuk catatan, reminder, dan alarm dengan integrasi Gmail dan Google Calendar.

## Tech Stack

- **Flutter** (Dart)
- **Material 3**
- **Riverpod** (State Management)
- **Hive** (Local Database)
- **Dio** (HTTP Client)
- **Supabase** (Backend & Auth)
- **Google APIs** (Gmail & Calendar)
- **android_alarm_manager_plus** (Alarm)
- **flutter_local_notifications** (Notifications)

## Minimum Requirements

- Android 10+ (API 29)
- Target SDK: Android 14 (API 34)

## Setup

1. Install dependencies:
```bash
flutter pub get
```

2. Generate Hive adapters:
```bash
flutter pub run build_runner build --delete-conflicting-outputs
```

3. Run the app:
```bash
flutter run
```

4. Build release APK:
```bash
flutter build apk --release --split-per-abi
```

## Project Structure

```
lib/
├── main.dart
├── app/
│   ├── app.dart
│   ├── theme.dart
│   └── routes.dart
├── core/
│   ├── database/
│   │   ├── hive_service.dart
│   │   └── boxes.dart
│   └── utils/
├── features/
│   ├── notes/
│   │   ├── models/
│   │   ├── providers/
│   │   ├── screens/
│   │   └── widgets/
│   ├── reminders/
│   └── home/
└── shared/
    └── widgets/
```

## Features (MVP)

### Phase 1 - Notes (Current) ✅
- Create, edit, delete notes
- Search notes (case-insensitive)
- Pin/Archive notes
- Custom categories
- Offline first

### Phase 2 - Reminders & Alarm (Coming Soon)
- Convert note to reminder
- Date/Time picker
- Repeat options
- Priority levels
- Alarm notifications
- Snooze functionality

### Phase 3 - Google Integration (Coming Soon)
- Google Sign-in
- Gmail API (send emails on reminder)
- Google Calendar API (create/update/delete events)

### Phase 4 - Cloud Sync (Coming Soon)
- Supabase authentication
- Cloud sync
- Conflict resolution
- Background sync

## Development

### Generate Code
```bash
flutter pub run build_runner watch
```

### Clean Build
```bash
flutter clean
flutter pub get
flutter pub run build_runner build --delete-conflicting-outputs
```

### Run Tests
```bash
flutter test
```

## Notes

- Phase 1 sudah selesai (Notes offline)
- Untuk testing di physical device, sambungkan Android 10+ device via USB
- Google API credentials diperlukan untuk Phase 3
- Supabase account diperlukan untuk Phase 4
