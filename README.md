<div align="center">

  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="art/logo_dark.png">
    <source media="(prefers-color-scheme: light)" srcset="art/logo_light.png">
    <img alt="dotnotes logo" src="art/logo.png" width="180">
  </picture>

  # .notes (dotnotes)
  
  **Aplikasi Catatan Harian & Pengingat Alarm Layar Penuh — Cepat, Bersih, dan Bebas Iklan.**

  [![Latest Release](https://img.shields.io/github/v/release/zaq-ly/dotnotes?style=flat-square&color=2563EB&label=Release)](https://github.com/zaq-ly/dotnotes/releases/latest)
  [![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white)](https://www.android.com)
  [![Kotlin](https://img.shields.io/badge/Kotlin-2.3.21-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org)
  [![Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202026.04.01-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
  [![License](https://img.shields.io/badge/License-MIT-gray?style=flat-square)](LICENSE)

  <br />

  <a href="https://github.com/zaq-ly/dotnotes/releases/latest">
    <img src="https://img.shields.io/badge/Unduh%20APK%20Terbaru-2563EB?style=for-the-badge&logo=android&logoColor=white" alt="Download APK" />
  </a>

</div>

---

## Tentang Aplikasi

**.notes (dotnotes)** adalah aplikasi catatan harian dan pengingat untuk platform Android native yang dirancang untuk kecepatan, kesederhanaan, dan privasi penuh.

Aplikasi ini mengusung konsep *offline-first*—dapat digunakan langsung tanpa koneksi internet dan tanpa kewajiban registrasi akun. Seluruh catatan tersimpan aman secara lokal di perangkat Anda, 100% gratis, dan sepenuhnya bebas iklan.

Bagi pengguna yang membutuhkan sinkronisasi lintas perangkat, tersedia opsi masuk dengan Akun Google untuk pencadangan otomatis ke cloud (Supabase) secara aman.

---

## Fitur Utama

| Fitur | Deskripsi |
| :--- | :--- |
| **Editor Catatan & Tugas Interaktif** | Pemformatan *rich text* (tebal, miring, daftar poin, penomoran), daftar tugas (*checklist to-do*), serta penghitung kata & karakter. |
| **8 Palet Warna Modern** | Personalisasi warna pada setiap kartu catatan yang adaptif dan nyaman untuk mode Terang (*Light*) maupun Gelap (*Dark*). |
| **Pengingat & Alarm Layar Penuh** | Pengingat interaktif satu layar penuh saat perangkat terkunci (*full-screen lockscreen intent*), lengkap dengan tombol Matikan (*Dismiss*) dan Tunda (*Snooze*). |
| **Pilihan Nada Alarm & Pengingat** | Pemilih nada dering sistem (*system ringtone/alarm picker*) bawaan perangkat untuk personalisasi suara pengingat. |
| **Pengingat Berulang Fleksibel** | Jadwal pengingat otomatis dengan interval: Sekali, Harian, Mingguan, Bulanan, atau Tahunan, lengkap dengan feedback jadwal pada kartu catatan. |
| **Riwayat & Pemantau Jadwal** | Halaman khusus dengan tab terpisah untuk memantau pengingat aktif yang akan datang dan riwayat alarm yang telah selesai. |
| **Notifikasi Persisten & Akses Cepat** | Notifikasi bilah status yang tahan usap (*swipe-proof*) dengan tombol aksi cepat *Tandai Selesai* dan *Tunda*. |
| **Sematkan Catatan (Pin)** | Menjaga catatan penting selalu berada di urutan teratas daftar catatan. |
| **Cadangkan & Pulihkan Lokal** | Ekspor dan impor seluruh data catatan ke format file JSON secara aman melalui *Storage Access Framework* (SAF). |
| **Sinkronisasi Cloud Supabase (Opsional)** | Hubungkan Akun Google untuk pencadangan otomatis dan sinkronisasi real-time berbasis PostgreSQL & *Row Level Security* (RLS). |
| **Pembaruan Langsung di Aplikasi** | Pengecekan versi baru via GitHub API di latar belakang (WorkManager) atau manual di menu Pengaturan, dengan instalasi instan via *FileProvider*. |
| **Antarmuka Bilingual & Pilihan Tema** | Dukungan penuh Bahasa Indonesia dan English, serta pilihan tema Terang (*Light*), Gelap (*Dark*), atau Mengikuti Sistem. |
| **100% Offline-First & Bebas Iklan** | Bekerja mandiri tanpa ketergantungan server, tanpa analitik pelacak, dan menjamin privasi catatan Anda. |

---

## Arsitektur & Teknologi

Dikembangkan menggunakan arsitektur modern Android (*MVVM + Repository Pattern*) dan library standar industri:

- **Bahasa & Compiler**: Kotlin 2.3.21 (KSP 2.3.11, JVM Target Java 17)
- **Desain Antarmuka**: Jetpack Compose (BOM 2026.04.01) + Material Design 3
- **Navigasi**: Jetpack Navigation Compose 2.9.8
- **Basis Data Lokal**: Room Database 2.8.4 (Schema v3) & DataStore Preferences 1.2.1
- **Rich Text Editor**: RichEditor Compose 1.0.0
- **Cloud Backend**: Supabase Kotlin SDK 3.1.1 (Auth & PostgREST), Ktor Client OkHttp 3.1.1, Kotlinx Serialization
- **Autentikasi**: AndroidX Credentials 1.6.0 + Google ID 1.2.0 + Play Services Auth 21.3.0
- **Sistem Pengingat**: Android AlarmManager (`setAlarmClock` / `setExactAndAllowWhileIdle`), Foreground Service, BroadcastReceiver
- **Tugas Latar Belakang**: AndroidX WorkManager 2.9.1 (Pemeriksa pembaruan berkala)
- **Target Platform**: Min SDK 26 (Android 8.0 Oreo), Target SDK 36 (Android 16), AGP 9.3.0

---

## Panduan Pemasangan

1. Kunjungi [**Halaman Rilis GitHub**](https://github.com/zaq-ly/dotnotes/releases/latest).
2. Unduh berkas APK versi terbaru (`dotnotes-vX.Y.Z.apk`).
3. Buka berkas unduhan pada perangkat Android Anda.
4. Jika muncul dialog keamanan instalasi, aktifkan opsi **"Izinkan dari sumber ini"** (*Allow from this source*).
5. Pilih **Install** dan buka aplikasi **.notes**.

> **Persyaratan Sistem**: Perangkat dengan sistem operasi **Android 8.0 (Oreo)** ke atas (API 26+).

---

## Izin Akses Sistem

Untuk memastikan alarm dan pengingat bekerja optimal tanpa terlewat, aplikasi memerlukan beberapa izin sistem:

- **Notifikasi (`POST_NOTIFICATIONS`)**: Menampilkan pengingat dan kontrol cepat di bilah status.
- **Alarm Tepat Waktu (`SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM`)**: Menjadwalkan alarm tepat pada detik yang ditentukan.
- **Tampilan Layar Penuh (`USE_FULL_SCREEN_INTENT`)**: Membuka antarmuka interaktif saat alarm berdering di kondisi layar terkunci.
- **Mulai Ulang Otomatis (`RECEIVE_BOOT_COMPLETED`)**: Menjadwalkan ulang pengingat aktif secara otomatis saat perangkat selesai dihidupkan ulang.
- **Instalasi Paket (`REQUEST_INSTALL_PACKAGES`)**: Memfasilitasi pembaruan aplikasi langsung dari menu Pengaturan.

---

## Kontribusi & Dukungan

- **Laporan Masalah & Saran Fitur**: Ajukan melalui [**GitHub Issues**](https://github.com/zaq-ly/dotnotes/issues).
- **Kontribusi Kode**: Ajukan *Pull Request* untuk perbaikan atau penambahan fitur.

---

<div align="center">
  <sub>Dibuat dengan kesederhanaan untuk kenyamanan mencatat harian.</sub>
</div>
