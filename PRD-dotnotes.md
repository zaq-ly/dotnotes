# PRD — .notes (dibaca "dot notes")

**Versi:** 1.0
**Tanggal:** 25 Agustus 2026
**Platform:** Android (Flutter, cross-platform ready)

---

## 1. Ringkasan Produk

**.notes** adalah aplikasi pencatat (notes) dengan sistem pengingat bertingkat (notifikasi, email, alarm) yang terintegrasi dengan akun Google user. Berbeda dari notes app pada umumnya, .notes fokus pada kepastian bahwa pengingat penting benar-benar sampai ke user — lewat kombinasi notifikasi lokal, email backup, dan alarm full-screen untuk item berprioritas tinggi.

### Masalah yang Diselesaikan
Notes app kebanyakan (Google Keep, dll) hanya mengandalkan notifikasi biasa yang mudah terlewat/di-dismiss tanpa sadar. .notes memberi user kontrol tingkat urgensi per catatan, sampai ke level alarm yang tidak bisa diabaikan begitu saja.

### Target User
Individu yang butuh sistem pengingat personal yang reliable — tugas, jadwal, deadline — dan ingin satu aplikasi yang menggabungkan catatan + reminder tanpa app terpisah-pisah.

---

## 2. Branding

- **Nama aplikasi:** .notes
- **Logo:** Monokrom hitam-putih, tipografi teks ".notes" dengan huruf 'o' dimodifikasi berbentuk lipatan kertas catatan (lihat `Logo dotnotes v.2.jpg`)
- **Accent color:** Monokrom / abu-abu netral (`#6B7280` — lihat `design-system.md`)
- **Catatan untuk AI coding agent:** Asset logo dikelola manual oleh developer dan akan ditempatkan langsung di folder asset project (mis. `assets/icon/`, `android/app/src/main/res/`). **Jangan generate/membuat asset logo secara otomatis** — cukup siapkan struktur folder/reference path-nya saja.

---

## 3. Fitur Utama

### 3.1 Manajemen Catatan
- Buat, edit, hapus catatan dengan **judul** dan **deskripsi**
- List catatan tersimpan, bisa di-scroll, sorted by terakhir diubah atau reminder terdekat

### 3.2 Sistem Pengingat Bertingkat
Setiap catatan bisa diberi reminder dengan **tanggal + jam**, dan salah satu dari 3 tingkat prioritas:

| Level | Nama | Perilaku |
|---|---|---|
| 1 | Notifikasi biasa | Push notification lokal saat waktu tiba via `flutter_local_notifications` |
| 2 | Notifikasi + Email | Notifikasi lokal + event Google Calendar dibuat otomatis dengan email reminder ke Gmail user |
| 3 | Notifikasi + Email + Alarm | Semua di atas + alarm full-screen native (`AlarmManager`), harus di-dismiss/snooze manual |

**Catatan teknis:** Alarm (level 3) dibangun native di dalam aplikasi menggunakan `AlarmManager`, bukan menyisipkan alarm ke aplikasi Clock bawaan HP.

### 3.3 Autentikasi & Sinkronisasi (100% Gratis / Zero Server)
- Login via Google Sign-In (OAuth scopes: `drive.appdata`, `calendar.events`)
- Catatan tersimpan di Google Drive pribadi user (folder tersembunyi `appDataFolder`), 0 biaya server untuk dev
- Mode offline-first: SQLite lokal (Drift) sebagai single source of truth, sync otomatis saat online/on-resume

### 3.4 Suara Notifikasi & Alarm
- Notifikasi dan alarm menggunakan sound default bawaan sistem Android (default notification sound & default alarm sound perangkat user)
- Tidak ada picker/kustomisasi sound per catatan — mengikuti pengaturan default HP masing-masing user

### 3.5 Tema Tampilan
- Mode gelap dan terang, mengikuti sistem atau bisa diatur manual

### 3.6 Bahasa
- Dukungan Bahasa Indonesia dan Inggris, bisa diganti dari Settings

---

## 4. User Flow Utama

1. **Onboarding:** User buka app → Login Google → sync Google Drive `appDataFolder` → masuk ke Home
2. **Buat catatan:** Home → tombol tambah → isi judul & deskripsi → (opsional) set reminder → pilih level prioritas → simpan
3. **Reminder jatuh tempo:**
   - Level 1: notifikasi muncul di device
   - Level 2: notifikasi muncul + Google Calendar otomatis kirim email pengingat ke Gmail user
   - Level 3: notifikasi + email Google Calendar + alarm full-screen berbunyi, user harus dismiss/snooze
4. **Kelola catatan:** Home → tap catatan → edit/hapus

---

## 5. Requirement Non-Fungsional

- **Reliability:** Alarm dan notifikasi harus tetap jalan meski aplikasi di-kill (memerlukan izin `SCHEDULE_EXACT_ALARM`, `USE_FULL_SCREEN_INTENT`, dan exclude battery optimization)
- **Offline-first:** Operasi CRUD catatan tidak bergantung koneksi internet
- **Serverless & 0 Cost:** Tidak menggunakan backend berbayar atau hosting dengan limit project (mengandalkan Google OAuth, Google Drive API, dan Google Calendar API)
- **Privasi:** Data catatan tersimpan di HP lokal dan Google Drive milik user sendiri

---

## 6. Skema Data (Lokal SQLite / Drift & Sync JSON)

```
notes
 - id (uuid)
 - title (text)
 - description (text)
 - reminder_datetime (timestamp, nullable)
 - priority_level (enum: normal | email | alarm)
 - google_calendar_event_id (text, nullable)
 - is_completed (boolean, default false)
 - is_synced (boolean, default false)
 - deleted_at (timestamp, nullable)
 - created_at (timestamp)
 - updated_at (timestamp)
```

---

## 7. Tech Stack

**Frontend & Logic**
- Flutter (Dart)
- State management: Riverpod
- Local storage: Drift (SQLite) — offline-first
- Notifikasi: `flutter_local_notifications`
- Alarm: `android_alarm_manager_plus`
- Auth & APIs: `google_sign_in`, `googleapis` (`drive_v3`, `calendar_v3`)
- Localization: `flutter_localizations` + `.arb` (en, id)
- Theming: `ThemeMode` + `SharedPreferences`

**Backend / Cloud (100% Free)**
- Google Drive API (`appDataFolder`) untuk sinkronisasi data antar device
- Google Calendar API untuk pengiriman reminder email tanpa server backend

---

## 8. Desain

Arah visual: **Minimalis**, monokrom. Detail lengkap ada di `design-system.md`.

---

## 9. Roadmap MVP

| Fase | Scope |
|---|---|
| 0 | Setup project Flutter, Google Sign-In, struktur folder, splash screen |
| 1 | CRUD catatan lokal (Drift) + Sync Google Drive (`appDataFolder`) |
| 2 | Reminder level 1 (notifikasi biasa via `flutter_local_notifications`) |
| 3 | Reminder level 3 (alarm full-screen native Android) |
| 4 | Reminder level 2 (integrasi Google Calendar API untuk email reminder) |
| 5 | Polish: dark/light mode, localization EN/ID, CI/CD auto release |

MVP mencakup semua fase di atas (Fase 0–5) sebelum rilis pertama.

MVP mencakup semua fase di atas (Fase 0–5) sebelum rilis pertama.

---

## 10. Out of Scope (v1.0)

- iOS (arsitektur disiapkan ready, tapi tidak jadi target rilis awal)
- Kolaborasi/share catatan antar user
- Rich text formatting / attachment gambar di catatan
- Widget home screen
