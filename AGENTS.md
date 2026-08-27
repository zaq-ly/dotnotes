# AGENTS.md — Panduan Standar Pengembangan, Commit & Rilis .notes (dotnotes)

Dokumen ini adalah instruksi operasional wajib untuk semua AI Agent yang bekerja di repositori **.notes (dotnotes)**.

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

### Tabel Pemetaan Tipe Commit ke Versi:

| Tipe Pekerjaan (Commit Prefix) | Deskripsi Berdasarkan Perintah User | Dampak Versi | Contoh Transisi Versi |
| :--- | :--- | :--- | :--- |
| **`feat!:`** atau **`breaking:`** | Perubahan besar/merombak arsitektur, migrasi struktur data yang memutus kompatibilitas, atau redesign total aplikasi. | **MAJOR** (`X.0.0`)<br>*Digit depan +1, minor & patch reset ke 0* | `1.1.0` ➔ `2.0.0` |
| **`feat:`** | **Menambahkan fitur fungsional baru** yang diminta user (misal: tambah bahasa baru, in-app updater, filter tag, ekspor/impor). | **MINOR** (`1.X.0`)<br>*Digit tengah +1, patch reset ke 0* | `1.1.0` ➔ `1.2.0` |
| **`fix:`** | Memperbaiki bug, salah hitung waktu/jam alarm, error crash, dsb. | **PATCH** (`1.0.X`)<br>*Digit belakang +1* | `1.1.0` ➔ `1.1.1` |
| **`style:`** / **`refactor:`** / **`perf:`** | Mempercantik tampilan (UI), mengubah tata letak tombol, membersihkan kode tanpa menambah fitur baru. | **PATCH** (`1.0.X`)<br>*Digit belakang +1* | `1.1.0` ➔ `1.1.1` |

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
Jalankan kompilasi Gradle:
```powershell
$env:JAVA_HOME = "C:\Program Files\Zulu\zulu-21"
.\gradlew.bat assembleDebug --no-daemon
```

### Langkah 3: Siapkan File Rilis APK
Copy APK hasil build ke root workspace dan beri nama sesuai versi:
```powershell
Copy-Item "app\build\outputs\apk\debug\app-debug.apk" "dotnotes-v.[VERSION_NAME].apk"
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
