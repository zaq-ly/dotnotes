# Architecture — .notes

**Referensi:** `PRD-dotnotes.md`, `design-system.md`
**Platform:** Flutter (Android, cross-platform ready)

---

## 1. Prinsip Utama

- **Offline-first**: semua operasi CRUD catatan jalan tanpa internet ke Drift SQLite lokal.
- **Serverless & 0 Cost**: sinkronisasi cloud menggunakan Google Drive user (`appDataFolder`), reminder email menggunakan Google Calendar API.
- **Clean-ish layering**: pisah presentation, domain (state/business logic), dan data — UI tidak boleh memanggil Google API / database langsung.
- **Single source of truth**: SQLite (Drift) lokal adalah sumber kebenaran utama untuk UI.

---

## 2. Struktur Folder

```
lib/
├── main.dart
├── app/
│   ├── app.dart                # MaterialApp, ThemeMode, routing
│   ├── router.dart              # Route definitions
│   └── theme/
│       ├── app_colors.dart      # Token warna dari design-system.md
│       ├── app_text_styles.dart
│       └── app_theme.dart       # ThemeData light/dark
│
├── core/
│   ├── constants/
│   ├── utils/
│   ├── errors/                  # Failure/Exception types
│   └── extensions/
│
├── data/
│   ├── local/
│   │   ├── drift/
│   │   │   ├── database.dart
│   │   │   └── tables/
│   │   │       └── notes_table.dart
│   │   └── preferences/         # Theme/locale prefs
│   ├── remote/
│   │   └── google/
│   │       ├── google_auth_client.dart
│   │       ├── google_drive_service.dart      # appDataFolder sync
│   │       └── google_calendar_service.dart   # Email reminder event
│   ├── repositories/
│   │   └── notes_repository_impl.dart
│   └── models/
│       └── note_model.dart      # mapping Drift <-> JSON Drive
│
├── domain/
│   ├── entities/
│   │   └── note.dart
│   ├── repositories/
│   │   └── notes_repository.dart   # abstract/interface
│   └── usecases/
│       ├── create_note.dart
│       ├── update_note.dart
│       ├── delete_note.dart
│       └── sync_notes.dart
│
├── features/
│   ├── auth/
│   │   ├── presentation/
│   │   └── application/         # Google Sign-In notifier
│   ├── notes_list/
│   │   ├── presentation/
│   │   └── application/
│   ├── note_editor/
│   │   ├── presentation/
│   │   └── application/
│   ├── reminder/
│   │   ├── notification_service.dart
│   │   ├── alarm_service.dart
│   │   └── application/
│   └── settings/
│       ├── presentation/
│       └── application/
│
└── l10n/
    ├── app_en.arb
    └── app_id.arb
```

**Aturan untuk AI agent:** widget di `features/*/presentation` hanya boleh memanggil provider di `application`, tidak boleh langsung akses `data/` atau Google APIs.

---

## 3. State Management (Riverpod)

- Tiap fitur punya `*Notifier` (AsyncNotifier) di folder `application/`.
- UI hanya `ref.watch` / `ref.read`.
- Usecase di `domain/usecases/` dipanggil dari notifier.

Alur satu arah:
```
UI (widget)
  → Notifier (application/)
    → UseCase (domain/usecases/)
      → Repository interface (domain/repositories/)
        → Repository impl (data/repositories/)
          → Local (Drift) + Cloud (Google Drive / Calendar)
```

---

## 4. Data Layer & Offline-First Strategy

- **Drift (SQLite)** = source of truth lokal. Semua UI membaca dari Drift.
- **Sync Flow (Google Drive `appDataFolder`):**
  1. Catatan dibuat/diedit/dihapus (soft-delete via `deleted_at`) di Drift + ditandai `is_synced = false`.
  2. Saat online / on resume / manual sync:
     - Baca state file snapshot `notes_backup.json` dari Google Drive `appDataFolder`.
     - Lakukan merge berdasarkan `updated_at` (last-write-wins per record ID).
     - Tulis hasil merge terbaru ke Google Drive & tandai `is_synced = true` di Drift.
- **Data Cleanup:** Record dengan `deleted_at != null` yang sudah tersinkron ke Drive dapat di-purge dari Drift lokal setelah periode tertentu.

---

## 5. Reminder & Alarm System

Tiga level reminder dipetakan ke service:

| Level | Trigger | Service |
|---|---|---|
| 1 — Notifikasi | Lokal HP saat waktu tiba | `flutter_local_notifications` via `NotificationService` |
| 2 — Notifikasi + Email | Notifikasi lokal + Event Google Calendar dengan reminder email | `GoogleCalendarService` (Google otomatis mengirim email ke Gmail user) |
| 3 — Notifikasi + Email + Alarm | Level 1 + Level 2 + Alarm full-screen native | `android_alarm_manager_plus` via `AlarmService` (Full-Screen Intent) |

**Penting (Android Requirements):**
- Di `AndroidManifest.xml` wajib dideklarasikan:
  - `android.permission.RECEIVE_BOOT_COMPLETED` (reschedule notif & alarm saat HP reboot)
  - `android.permission.SCHEDULE_EXACT_ALARM` & `android.permission.USE_EXACT_ALARM`
  - `android.permission.USE_FULL_SCREEN_INTENT`
  - `android.permission.POST_NOTIFICATIONS`
  - `android.permission.WAKE_LOCK`

---

## 6. Auth & Google Scopes

- `google_sign_in` meminta scopes:
  - `https://www.googleapis.com/auth/drive.appdata` (akses hidden folder aplikasi)
  - `https://www.googleapis.com/auth/calendar.events` (buat reminder email di kalender)
- Initial sync langsung berjalan otomatis setelah login sukses.

---

## 7. Theming & Localization

- Token warna/spacing/font di `app/theme/` mengikuti `design-system.md` monokrom (`#6B7280`, Inter).
- `ThemeMode.system` default, disimpan di `SharedPreferences`.
- Localization pakai `.arb` (`en`, `id`) via `AppLocalizations.of(context)`.

---

## 8. Versioning

- Versi app mengikuti `pubspec.yaml` (`version: x.y.z+build`), dibaca runtime lewat `package_info_plus`.
- Source of truth versi = tag GitHub Release via GitHub Actions (`RELEASE-WORKFLOW.md`).

---

## 9. Catatan untuk AI Coding Agent

- Jangan taruh Google API call langsung di widget — selalu lewat repository → usecase → notifier.
- Jangan generate asset logo otomatis (gunakan aset yang sudah disiapkan developer).
- Semua string user-facing wajib lewat `.arb`.
