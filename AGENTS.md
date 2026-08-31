# AGENTS.md — Panduan Standar Pengembangan, Commit & Rilis .notes (dotnotes)

Dokumen ini adalah instruksi operasional wajib untuk semua AI Agent yang bekerja di repositori **.notes (dotnotes)**.

---

## 0. Aturan Komunikasi & Mode Diskusi (Wajib Diikuti)

1. **Mode Diskusi (Zero Code Execution)**:
   - Setiap kali user menyebut kata **"diskusi"**, meminta penjelasan, atau mengajak merundingkan fitur/alur:
   - AI Agent **DILARANG KERAS mengubah, membuat, atau mengeksekusi kode apa pun**.
   - Fokus 100% pada penjabaran ide, konsep arsitektur, diagram alur, dan analisis teknis.
   - Pengerjaan kode **HANYA BOLEH DIMULAI** setelah user secara eksplisit memberikan instruksi persetujuan (misal: *"oke kerjakan"*, *"terapkan"*, atau *"lanjut eksekusi"*).

2. **Aturan Commit & Push Mandiri**:
   - Dilarang keras melakukan `git commit`, `git push`, atau membuat GitHub Release otomatis tanpa perintah eksplisit dari user.
   - Pengujian lokal hanya dilakukan via compile APK dan push langsung ke HP via adb debugging.

---

## 1. Ikhtisar Proyek & Arsitektur
- **Platform**: Android Native (Min SDK 26, Target SDK 34)
- **Bahasa & UI**: Kotlin 2.0 + Jetpack Compose (Material3)
- **Penyimpanan Lokal**: Room Database + DataStore Preferences
- **Prinsip Utama**: 100% Gratis, *Offline-First*, Bebas Server/Birokrasi Cloud, Privasi Terjaga.
- **Fitur Kunci**:
  - Catatan Rich Text (Bold, Italic, Bulleted/Numbered List).
  - Pengingat & Alarm Layar Penuh (*Full-screen Intent* + *Heads-up Notification*).
  - Backup & Restore Mandiri (File JSON lokal via Android Storage Access Framework).
  - In-App Updater langsung dari GitHub Release API.
  - Multi-bahasa (*English* & *Bahasa Indonesia* via `LocalStrings`).

---

## 2. Logika Penentuan Pesan Commit & Versi Otomatis

AI Agent **wajib menganalisis instruksi/permintaan user** untuk menyusun pesan commit terlebih dahulu, kemudian **menurunkan penomoran versi (SemVer) dari tipe commit tersebut**:

```text
Permintaan User ───> Analisis Tipe Pekerjaan ───> Rumuskan Pesan Commit ───> Tentukan Versi (MAJOR / MINOR / PATCH)
```

### Prinsip Kenaikan Versi SemVer Murni:
1. **PATCH (`X.Y.Z`)**:
   - Digit paling belakang (`Z`) bertambah `+1` untuk setiap pekerjaan `fix:`, `style:`, `refactor:`, `perf:`, atau penyesuaian fungsional kecil.
   - **Bebas naik tanpa batas digit (`1.20.99` ➔ `1.20.100` ➔ `1.20.1000`)** selama masih dalam lingkup perbaikan/refactor.
   - **DILARANG KERAS** menaikkan angka MINOR secara otomatis hanya karena angka PATCH sudah besar.
2. **MINOR (`X.Y.0`)**:
   - Digit tengah (`Y`) **HANYA BOLEH NAIK** jika ada **penambahan modul fitur baru berskala besar** atau atas **perintah eksplisit dari user**.
   - Saat MINOR naik, digit PATCH di-reset ke `0` (contoh: `1.20.1000` ➔ `1.21.0`).
3. **MAJOR (`X.0.0`)**:
   - Digit depan (`X`) **HANYA BOLEH NAIK** jika ada **perombakan arsitektur besar / breaking changes** atau atas **perintah eksplisit dari user**.
   - Saat MAJOR naik, digit MINOR & PATCH di-reset ke `0` (contoh: `1.20.1000` ➔ `2.0.0`).

### Tabel Pemetaan Tipe Commit ke Versi:

| Tipe Pekerjaan (Commit Prefix) | Deskripsi Berdasarkan Perintah User | Dampak Versi | Contoh Transisi Versi |
| :--- | :--- | :--- | :--- |
| **`feat!:`** atau **`breaking:`** | Perubahan besar/merombak arsitektur, migrasi struktur data yang memutus kompatibilitas, atau instruksi eksplisit user. | **MAJOR** (`X.0.0`)<br>*Digit depan +1, minor & patch reset ke 0* | `1.20.500` ➔ `2.0.0` |
| **`feat:`** | **Menambahkan modul/fitur fungsional baru** (misal: tambah modul sinkronisasi baru, export/import engine baru, dsb.) atau instruksi user. | **MINOR** (`1.X.0`)<br>*Digit tengah +1, patch reset ke 0* | `1.20.500` ➔ `1.21.0` |
| **`fix:`** | Memperbaiki bug, salah hitung waktu/jam alarm, error crash, tampilan terpotong, dsb. | **PATCH** (`1.X.Z`)<br>*Digit belakang +1 (tanpa batas)* | `1.20.6` ➔ `1.20.7`<br>`1.20.99` ➔ `1.20.100`<br>`1.20.999` ➔ `1.20.1000` |
| **`style:`** / **`refactor:`** / **`perf:`** | Mempercantik tampilan (UI), mengubah padding/posisi tombol, optimasi performa, membersihkan kode tanpa modul baru. | **PATCH** (`1.X.Z`)<br>*Digit belakang +1 (tanpa batas)* | `1.20.6` ➔ `1.20.7` |

### Format Standar Pesan Commit:
Format commit wajib menggunakan standar Conventional Commits yang mencerminkan instruksi user:
- `feat: [fitur baru sesuai permintaan user]`
- `fix: [masalah yang diperbaiki]`
- `style: [bagian UI yang dipercantik/diubah posisinya]`
- `refactor: [kode yang dibersihkan/disederhanakan]`

---

## 3. SOP Rilis & Pembaruan Versi (Langkah Demi Langkah)

Setiap kali user meminta **"commit"**, **"rilis"**, atau **"update versi"**, jalankan urutan ini:

### Langkah 1: Rumuskan Commit & Update Versi di File Konfigurasi
1. Tentukan pesan commit sesuai instruksi user (misal: `fix: adjust reminder time display and floating notification button`).
2. Evaluasi tipe commit (`feat` / `fix` / `style` / dsb.) untuk menentukan versi baru (`MAJOR`, `MINOR`, atau `PATCH`).
3. Buka `app/build.gradle.kts`:
   - `versionCode`: Tambah `+1` (integer) dari nilai sebelumnya.
   - `versionName`: Perbarui ke versi baru (contoh: `"1.1.1"` atau `"1.2.0"`).
4. Buka `app/src/main/java/com/dotnotes/app/ui/screens/settings/SettingsScreen.kt`:
   - Sinkronkan string versi di item `About` (contoh: `"dotnotes v[VERSION_NAME]"`).
   - Sinkronkan argumen fungsi `viewModel.checkForUpdate("[VERSION_NAME]")`.

### Langkah 2: Build APK Lokal
Jalankan kompilasi Gradle tipe Release bertanda tangan resmi (*signed release*):
```powershell
$env:JAVA_HOME = "C:\Program Files\Zulu\zulu-21"
.\gradlew.bat assembleRelease --no-daemon
```

### Langkah 3: Siapkan File Rilis APK
Copy APK release hasil build ke root workspace dan beri nama sesuai versi:
```powershell
Copy-Item "app\build\outputs\apk\release\app-release.apk" "dotnotes-v.[VERSION_NAME].apk"
```

### Langkah 4: Git Commit & Push
Commit seluruh kode sumber yang telah dimodifikasi menggunakan pesan commit yang telah dirumuskan:
```powershell
git add .
git commit -m "Release v[VERSION_NAME] - [Tipe]: [Deskripsi sesuai instruksi user]"
git push
```

### Langkah 5: Publikasikan ke GitHub Release
Unggah file APK dan buat rilis publik menggunakan GitHub CLI (`gh`):
```powershell
gh release create v[VERSION_NAME] dotnotes-v.[VERSION_NAME].apk --title "Release v[VERSION_NAME]" --notes "Update to version [VERSION_NAME] - [Deskripsi Perubahan]"
```

### Langkah 6: Pembersihan (Cleanup)
Hapus file APK lokal di root folder:
```powershell
Remove-Item "dotnotes-v.[VERSION_NAME].apk"
```

---

## 4. Aturan Desain & Kode untuk AI Agent
1. **UI Bersih & Seamless**: Hindari border kotak tebal pada teks input; utamakan tipografi modern dan *borderless editor*.
2. **Multi-Bahasa**: Semua teks baru yang ditampilkan ke user **wajib didaftarkan** di [Localization.kt](file:///c:/Users/ACER/.gemini/antigravity/scratch/dotnotes/app/src/main/java/com/dotnotes/app/ui/i18n/Localization.kt) untuk bahasa Inggris (`EnglishStrings`) dan Indonesia (`IndonesianStrings`).
3. **Penanganan Notifikasi**: Selalu sertakan tombol aksi langsung seperti *"Tandai Selesai"* dan *"Tunda"* pada notifikasi pengingat & alarm.
4. **Git Hygiene**: Jangan pernah menambahkan file `.apk` ke dalam commit Git biasa (file `.apk` hanya diunggah ke GitHub Releases via `gh`).
