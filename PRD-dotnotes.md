# PRD — .notes (dibaca "dot notes")

**Versi:** 1.0
**Tanggal:** 21 Agustus 2026
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
- **Logo:** Ikon notebook + pena, gradient biru, background putih rounded-square
- **Accent color:** Biru (diturunkan dari logo — lihat `design-system.md`)
- **Catatan untuk AI coding agent:** Asset logo (app icon, splash screen, dsb.) dikelola manual oleh developer dan akan ditempatkan langsung di folder asset project (mis. `assets/icon/`, `android/app/src/main/res/`). **Jangan generate/membuat asset logo secara otomatis** — cukup siapkan struktur folder/reference path-nya saja.

---

## 3. Fitur Utama

### 3.1 Manajemen Catatan
- Buat, edit, hapus catatan dengan **judul** dan **deskripsi**
- List catatan tersimpan, bisa di-scroll, sorted by terakhir diubah atau reminder terdekat

### 3.2 Sistem Pengingat Bertingkat
Setiap catatan bisa diberi reminder dengan **tanggal + jam**, dan salah satu dari 3 tingkat prioritas:

| Level | Nama | Perilaku |
|---|---|---|
| 1 | Notifikasi biasa | Push notification lokal saat waktu tiba |
| 2 | Notifikasi + Email | Notifikasi lokal + email dikirim ke akun Google user |
| 3 | Notifikasi + Email + Alarm | Semua di atas + alarm full-screen (mirip alarm bawaan HP), harus di-dismiss/snooze manual |

**Catatan teknis:** Alarm (level 3) dibangun native di dalam aplikasi menggunakan `AlarmManager`, bukan menyisipkan alarm ke aplikasi Clock bawaan HP (keterbatasan platform Android — third-party app tidak bisa menulis langsung ke app Clock sistem).

### 3.3 Autentikasi & Sinkronisasi
- Login via Google Sign-In
- Catatan tersimpan di cloud (Supabase), tersinkron antar device dengan akun sama
- Mode offline-first: catatan tetap bisa dibuat/diedit tanpa internet, sync otomatis saat online kembali

### 3.4 Suara Notifikasi & Alarm
- Notifikasi dan alarm menggunakan sound default bawaan sistem Android (default notification sound & default alarm sound perangkat user)
- Tidak ada picker/kustomisasi sound per catatan — mengikuti pengaturan default HP masing-masing user

### 3.5 Tema Tampilan
- Mode gelap dan terang, mengikuti sistem atau bisa diatur manual

### 3.6 Bahasa
- Dukungan Bahasa Indonesia dan Inggris, bisa diganti dari Settings

---

## 4. User Flow Utama

1. **Onboarding:** User buka app → Login Google → masuk ke Home
2. **Buat catatan:** Home → tombol tambah → isi judul & deskripsi → (opsional) set reminder → pilih level prioritas → simpan (sound notifikasi/alarm otomatis pakai default HP, tidak perlu dipilih manual)
3. **Reminder jatuh tempo:**
   - Level 1: notifikasi muncul di device
   - Level 2: notifikasi muncul + email masuk ke Gmail user
   - Level 3: notifikasi + email + alarm full-screen berbunyi, user harus dismiss/snooze
4. **Kelola catatan:** Home → tap catatan → edit/hapus

---

## 5. Requirement Non-Fungsional

- **Reliability:** Alarm dan notifikasi harus tetap jalan meski aplikasi di-kill (memerlukan exclude dari battery optimization, khususnya OEM seperti Xiaomi/Samsung yang agresif membunuh background process)
- **Offline-first:** Operasi CRUD catatan tidak boleh bergantung pada koneksi internet
- **Email delivery:** Pengiriman email harus dari server (bukan device), karena device bisa saja mati/offline saat waktu reminder tiba
- **Privasi:** Data catatan user hanya bisa diakses oleh user pemilik akun (row-level security di database)

---

## 6. Skema Data (Kasar)

```
notes
 - id (uuid)
 - user_id (uuid, FK ke auth.users)
 - title (text)
 - description (text)
 - reminder_datetime (timestamp, nullable)
 - priority_level (enum: normal | email | alarm)
 - is_completed (boolean, default false)
 - created_at (timestamp)
 - updated_at (timestamp)
```

---

## 7. Tech Stack

**Frontend**
- Flutter (Dart)
- State management: Riverpod
- Local storage: Drift (SQLite) — offline-first
- Notifikasi: `flutter_local_notifications`
- Alarm: `android_alarm_manager_plus`
- Auth: `google_sign_in`
- Localization: `flutter_localizations` + `.arb` (en, id)
- Theming: `ThemeMode` + `SharedPreferences`

**Backend**
- Supabase: Auth (Google OAuth), Postgres, Edge Functions, `pg_cron` (cek reminder tiap menit untuk trigger email)
- Resend: layanan pengiriman email

---

## 8. Desain

Arah visual: **Minimalis**, terinspirasi Notion/Google Keep. Detail lengkap (palet warna, tipografi, komponen) ada di dokumen terpisah: `design-system.md`.

---

## 9. Roadmap MVP

| Fase | Scope |
|---|---|
| 0 | Setup project, Supabase, Google Sign-In, splash screen |
| 1 | CRUD catatan + sync Supabase |
| 2 | Reminder level 1 (notifikasi biasa) |
| 3 | Reminder level 3 (alarm full-screen) |
| 4 | Reminder level 2 (email via Edge Function + Resend) |
| 5 | Polish: dark/light mode, localization EN/ID |

MVP mencakup semua fase di atas (Fase 0–5) sebelum rilis pertama.

---

## 10. Out of Scope (v1.0)

- iOS (arsitektur disiapkan ready, tapi tidak jadi target rilis awal)
- Kolaborasi/share catatan antar user
- Rich text formatting / attachment gambar di catatan
- Widget home screen
