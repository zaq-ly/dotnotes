<div align="center">

  <img src="art/logo.jpg" alt="dotnotes logo" width="120" style="border-radius: 24px; box-shadow: 0 4px 16px rgba(0,0,0,0.12);" />

  # .notes (dotnotes)
  
  **Aplikasi Catatan Harian & Pengingat Alarm Layar Penuh — Cepat, Bersih, dan Bebas Iklan.**

  [![Latest Release](https://img.shields.io/github/v/release/zaq-ly/dotnotes?style=flat-square&color=2563EB&label=Release)](https://github.com/zaq-ly/dotnotes/releases/latest)
  [![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white)](https://www.android.com)
  [![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org)
  [![License](https://img.shields.io/badge/License-MIT-gray?style=flat-square)](LICENSE)

  <br />

  <a href="https://github.com/zaq-ly/dotnotes/releases/latest">
    <img src="https://img.shields.io/badge/Unduh%20APK%20Terbaru-2563EB?style=for-the-badge&logo=android&logoColor=white" alt="Download APK" />
  </a>

</div>

---

## Tentang Aplikasi

**.notes (dotnotes)** adalah aplikasi catatan harian untuk Android yang dirancang untuk kecepatan, kemudahan menulis, dan privasi penuh.

Aplikasi ini dapat langsung digunakan tanpa koneksi internet dan tanpa kewajiban mendaftar akun. Seluruh data tersimpan secara lokal di perangkat pengguna, sepenuhnya gratis, dan bebas iklan.

Tersedia opsi masuk dengan Akun Google bagi pengguna yang ingin mencadangkan dan menyinkronkan catatan antar perangkat secara aman.

---

## Fitur Utama

| Fitur | Deskripsi |
| :--- | :--- |
| **Editor Catatan & Daftar Tugas** | Pemformatan teks kaya (tebal, miring, daftar poin, nomor) dan kotak centang tugas harian yang interaktif. |
| **8 Palet Warna Catatan** | Personalisasi warna pada setiap kartu catatan untuk kemudahan identifikasi visual dan pengelompokan. |
| **Alarm Layar Penuh** | Pengingat interaktif satu layar penuh saat perangkat terkunci, dilengkapi tombol Matikan (*Dismiss*) dan Tunda (*Snooze*). |
| **Pengingat Berulang** | Jadwal alarm fleksibel dengan interval otomatis: Sekali, Harian, Mingguan, Bulanan, atau Tahunan. |
| **Riwayat & Pantauan Jadwal** | Halaman khusus dengan tab terpisah untuk memantau pengingat yang sedang aktif dan riwayat alarm selesai. |
| **Sematkan Catatan (Pin)** | Menjaga catatan penting selalu berada di urutan teratas daftar. |
| **Sinkronisasi Akun Google (Opsional)** | Hubungkan akun untuk pencadangan otomatis dan pemulihan data yang aman. |
| **Offline-First & Bebas Iklan** | Bekerja mandiri tanpa ketergantungan server, tanpa analitik pihak ketiga, dan tanpa iklan. |
| **Pembaruan Langsung di Aplikasi** | Memeriksa dan mengunduh versi terbaru secara langsung melalui menu Pengaturan. |
| **Pilihan Bahasa & Tampilan** | Antarmuka bilingual (Bahasa Indonesia & English) serta dukungan mode Terang, Gelap, dan Sistem. |

---

## Arsitektur & Teknologi

Aplikasi ini dikembangkan menggunakan standar modern pengembangan aplikasi Android:

- **Bahasa Utama**: Kotlin 2.0 (Resmi dari Google untuk Android)
- **Desain Antarmuka**: Jetpack Compose dengan Material Design 3
- **Basis Data Lokal**: Room Database (Cepat, aman, dan hemat baterai)
- **Layanan Sinkronisasi**: Supabase Engine & Google Sign-In
- **Sistem Pengingat**: Android AlarmManager & Notification Engine

---

## Panduan Pemasangan

1. Kunjungi [**Halaman Rilis GitHub**](https://github.com/zaq-ly/dotnotes/releases/latest).
2. Unduh berkas installer berekstensi `.apk` versi terbaru.
3. Buka berkas unduhan pada perangkat Android Anda.
4. Jika muncul dialog keamanan, aktifkan opsi **"Izinkan dari sumber ini"** (*Allow from this source*).
5. Pilih **Install** dan buka aplikasi **.notes**.

> **Persyaratan Sistem**: Perangkat dengan sistem operasi **Android 8.0 (Oreo)** ke atas.

---

## Izin Akses Sistem

Untuk memastikan fungsi pengingat dan alarm bekerja optimal, aplikasi memerlukan beberapa izin sistem:

- **Notifikasi**: Menampilkan pemberitahuan dan kontrol cepat pengingat di bilah status.
- **Tampilan Layar Penuh**: Membuka tampilan interaktif saat alarm berdering ketika layar ponsel terkunci.
- **Mulai Ulang Otomatis**: Memulihkan jadwal pengingat secara otomatis saat perangkat selesai dihidupkan ulang.

---

## Kontribusi & Dukungan

- **Laporan Masalah & Saran**: Ajukan melalui [**GitHub Issues**](https://github.com/zaq-ly/dotnotes/issues).
- **Pengembangan Kode**: Buka *Pull Request* baru untuk kontribusi fitur atau perbaikan kode.

---

<div align="center">
  <sub>Dikembangkan untuk kemudahan dan kenyamanan mencatat harian.</sub>
</div>
