<div align="center">

  <img src="art/logo.jpg" alt="dotnotes logo" width="130" style="border-radius: 28px; box-shadow: 0 4px 12px rgba(0,0,0,0.15);" />

  # .notes (dotnotes)
  
  **Aplikasi Catatan Modern, Offline-First dengan Pengingat & Notifikasi Alarm Mengambang.**

  [![Latest Release](https://img.shields.io/github/v/release/zaq-ly/dotnotes?style=for-the-badge&color=2563EB&label=Release)](https://github.com/zaq-ly/dotnotes/releases/latest)
  [![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://www.android.com)
  [![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
  [![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
  [![License](https://img.shields.io/badge/License-MIT-emerald?style=for-the-badge)](LICENSE)

  <br />

  <a href="https://github.com/zaq-ly/dotnotes/releases/latest">
    <img src="https://img.shields.io/badge/⬇️%20DOWNLOAD-APK%20TERBARU-2563EB?style=for-the-badge&logo=android&logoColor=white" alt="Download APK" />
  </a>

</div>

---

## 📖 Tentang Aplikasi

**.notes (dotnotes)** adalah aplikasi catatan Android yang dirancang dengan fokus pada kesederhanaan, kecepatan, dan privasi penuh. Berbeda dengan aplikasi catatan berbasis cloud, **.notes** berjalan **100% secara offline** di perangkat Anda—tanpa iklan, tanpa pelacakan data, dan tanpa perlu membuat akun.

Aplikasi ini memadukan kemudahan menulis catatan berformat kaya (*Rich Text*) dan daftar tugas (*to-do list*) dengan sistem notifikasi pengingat mengambang (*Heads-up Notification*) yang memanfaatkan nada dering alarm bawaan perangkat Anda.

---

## ✨ Fitur Unggulan

| Fitur | Deskripsi |
| :--- | :--- |
| ✍️ **Rich Text & To-Do Editor** | Tulis catatan dengan kotak centang (*checkbox* / daftar tugas), format tebal (*bold*), miring (*italic*), serta daftar bertitik (*bulleted*) dan bernomor (*numbered list*). |
| ⏰ **Pengingat & Alarm Mengambang** | Pengingat tepat waktu berupa notifikasi mengambang (*Heads-up Notification*) dengan nada dering alarm default perangkat, dilengkapi tombol **Tandai Selesai** dan **Tunda (Snooze)** langsung dari bilah notifikasi. |
| 🔒 **100% Offline & Privasi Terjaga** | Seluruh data catatan dan pengaturan tersimpan secara lokal di database perangkat Anda. Tidak ada server perantara atau analitik data eksternal. |
| 💾 **Backup & Restore Mandiri** | Ekspor dan impor seluruh data catatan Anda ke format file JSON lokal dengan aman menggunakan Android *Storage Access Framework* (SAF). |
| 🔄 **In-App Updater** | Terintegrasi langsung dengan GitHub Releases API untuk memeriksa versi baru dan memperbarui aplikasi langsung dari dalam menu pengaturan. |
| 🌐 **Multi-Bahasa (Bilingual)** | Mendukung antarmuka dalam **Bahasa Indonesia** dan **English**. |
| 🎨 **Desain Modern Material 3** | Tampilan antarmuka bersih, adaptif dengan dukungan tema Gelap (*Dark Mode*) dan Terang (*Light Mode*). |

---

## 🛠️ Stack Teknologi

Aplikasi ini dibangun menggunakan arsitektur modern Android (*Modern Android Development / MAD*):

- **Bahasa Pemrograman**: [Kotlin](https://kotlinlang.org/) (v2.0+)
- **Framework UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose) dengan [Material Design 3](https://m3.material.io/)
- **Arsitektur**: MVVM (Model-View-ViewModel) + Single Activity Architecture + Unidirectional Data Flow (UDF)
- **Basis Data Lokal**: [Room Database](https://developer.android.com/training/data-storage/room) didukung oleh KSP (Kotlin Symbol Processing)
- **Penyimpanan Pengaturan**: [Jetpack DataStore Preferences](https://developer.android.com/topic/libraries/architecture/datastore)
- **Rich Text Engine**: `richeditor-compose`
- **Sistem Alarm & Notifikasi**: `AlarmManager` (Exact Alarm), `BroadcastReceiver`, `Foreground Service`, & `NotificationManager`
- **Build System**: Gradle Kotlin DSL (`build.gradle.kts`)

---

## 📥 Cara Download & Instalasi

Anda dapat mengunduh file installer APK resmi langsung dari repositori GitHub ini:

1. Kunjungi halaman rilis resmi di [**GitHub Releases**](https://github.com/zaq-ly/dotnotes/releases/latest).
2. Di bagian **Assets**, klik file berekstensi `.apk` (misalnya: `dotnotes-v.1.10.1.apk`).
3. Setelah proses unduh selesai, buka file APK tersebut di ponsel Android Anda.
4. Jika muncul peringatan keamanan, pilih **"Izinkan dari sumber ini"** (*Allow from this source*).
5. Tekan **Install** dan buka aplikasi **.notes**.

> 💡 **Persyaratan Sistem**: Perangkat Android dengan minimal **Android 8.0 (Oreo / API Level 26)** ke atas.

---

## 🔐 Izin Perangkat (Permissions)

Untuk menjalankan fitur alarm dan pengingat secara optimal, aplikasi memerlukan izin berikut:

- `POST_NOTIFICATIONS`: Menampilkan notifikasi pengingat mengambang dan kontrol alarm di bilah status.
- `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM`: Memastikan pengingat berdering tepat pada waktu yang ditentukan.
- `FOREGROUND_SERVICE` & `VIBRATE`: Menjalankan pemutaran nada dering alarm dan getaran saat pengingat aktif.
- `RECEIVE_BOOT_COMPLETED`: Menjadwalkan ulang pengingat yang tertunda secara otomatis setelah perangkat direstart.

---

## 🤝 Kontribusi & Laporan Masalah

Masukan, saran fitur, dan kontribusi kode selalu terbuka!
- Temukan bug atau ingin usul fitur baru? Buka [**GitHub Issues**](https://github.com/zaq-ly/dotnotes/issues).
- Ingin berkontribusi kode? Silakan *fork* repositori ini, buat branch fitur baru, dan ajukan *Pull Request*.

---

<div align="center">
  <sub>Dikembangkan dengan ❤️ untuk kemudahan mencatat harian secara mandiri & aman.</sub>
</div>
