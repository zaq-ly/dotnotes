# Architecture — .notes

**Referensi:** `PRD-dotnotes.md`, `design-system.md`
**Platform:** Flutter (Android, cross-platform ready)

---

## 1. Prinsip Utama

- **Offline-first**: semua operasi CRUD catatan jalan tanpa internet, sync ke Supabase saat online.
- **Clean-ish layering**: pisah presentation, domain (state/business logic), dan data — supaya AI coding agent tidak menaruh logic sembarangan di widget.
- **Single source of truth per data**: state lokal (Drift) adalah sumber kebenaran untuk UI, Supabase adalah sumber kebenaran untuk sync antar device.

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
│   ├── remote/
│   │   └── supabase/
│   │       ├── supabase_client.dart
│   │       └── notes_remote_datasource.dart
│   ├── repositories/
│   │   └── notes_repository_impl.dart
│   └── models/
│       └── note_model.dart      # mapping local <-> remote
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
│   │   └── application/         # Riverpod providers/notifiers
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

**Aturan untuk AI agent:** widget di `features/*/presentation` hanya boleh manggil provider di `application`, tidak boleh langsung akses `data/` atau Supabase client.

---

## 3. State Management (Riverpod)

- Tiap fitur punya `*Notifier` (StateNotifier/AsyncNotifier) di folder `application/`.
- UI hanya `ref.watch` / `ref.read`, tidak ada business logic di widget.
- Usecase di `domain/usecases/` dipanggil dari notifier, bukan langsung repository — supaya logic bisa dites terpisah dari UI.

Alur satu arah:
```
UI (widget)
  → Notifier (application/)
    → UseCase (domain/usecases/)
      → Repository interface (domain/repositories/)
        → Repository impl (data/repositories/)
          → Local (Drift) + Remote (Supabase)
```

---

## 4. Data Layer & Offline-First Strategy

- **Drift (SQLite)** = source of truth lokal. Semua read UI ambil dari Drift, bukan langsung dari Supabase.
- **Sync flow:**
  1. User create/edit/delete note → langsung ditulis ke Drift + ditandai `is_synced = false`.
  2. Background sync (saat online / on app resume) push perubahan `is_synced = false` ke Supabase.
  3. Pull perubahan dari Supabase (berdasarkan `updated_at`) → merge ke Drift.
- **Konflik sederhana:** last-write-wins berdasarkan `updated_at` (cukup untuk single-user, tidak ada kolaborasi di scope v1).
- Kolom tambahan di tabel lokal (tidak ada di skema Supabase PRD) yang perlu ditambahkan: `is_synced` (bool), `is_deleted` (bool, untuk soft-delete sebelum sync penghapusan).

---

## 5. Reminder & Alarm System

Tiga level reminder dari PRD dipetakan ke service terpisah:

| Level | Trigger | Service |
|---|---|---|
| 1 — Notifikasi | Lokal, dijadwalkan saat note disimpan | `flutter_local_notifications` via `NotificationService` |
| 2 — Notifikasi + Email | Notifikasi lokal (sama seperti di atas) + email dikirim dari **server** | Email dikirim oleh Supabase Edge Function + `pg_cron`, bukan dari device |
| 3 — Notifikasi + Email + Alarm | Semua di atas + alarm full-screen | `android_alarm_manager_plus` via `AlarmService`, buka full-screen intent |

**Penting:**
- Penjadwalan alarm/notifikasi lokal harus di-reschedule tiap kali app start (karena `AlarmManager` bisa hilang saat reboot) → tangani di `main.dart` atau service init.
- Email (level 2) **tidak boleh** bergantung pada device — logic pengecekan reminder due ada di `pg_cron` sisi Supabase, bukan di app.

---

## 6. Auth & Sync Trigger

- `google_sign_in` → dapat token → login ke Supabase Auth (Google OAuth).
- Setelah login sukses, trigger initial sync (pull semua notes user dari Supabase ke Drift lokal).
- Row-level security di Supabase memastikan user hanya bisa akses notes miliknya sendiri (sudah dicatat di PRD, tidak perlu ditangani di client, tapi client tetap wajib kirim `user_id` yang benar).

---

## 7. Theming & Localization

- Token warna/spacing/font di `app/theme/` mengikuti `design-system.md` — **jangan hardcode hex/size langsung di widget**.
- `ThemeMode.system` default, override manual disimpan di `SharedPreferences` (bukan Drift, karena ini preference device bukan data user).
- Localization pakai `flutter_localizations` + `.arb` (`en`, `id`), akses teks selalu lewat `AppLocalizations.of(context)`, tidak ada hardcoded string di widget.

---

## 8. Versioning

- Versi app mengikuti `pubspec.yaml` (`version: x.y.z+build`), dibaca runtime lewat `package_info_plus` (ditampilkan di halaman Settings).
- Source of truth versi = tag GitHub Release, disinkronkan ke `pubspec.yaml` lewat CI (lihat dokumen terpisah: workflow auto-release).

---

## 9. Catatan untuk AI Coding Agent

- Jangan taruh Supabase client call di widget — selalu lewat repository → usecase → notifier.
- Jangan generate asset logo (sudah dicatat di PRD/design-system).
- Kalau ada kebutuhan skema tabel baru/kolom baru di luar yang tercantum di sini dan di PRD, **tanya dulu**, jangan asumsi.
- Semua string user-facing wajib lewat `.arb`, tidak ada hardcoded text.
