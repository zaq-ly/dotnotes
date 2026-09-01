# AGENTS.md — Panduan Standar Pengembangan, Arsitektur & Rilis .notes (dotnotes)

Dokumen ini adalah instruksi operasional wajib dan panduan arsitektur lengkap untuk semua AI Agent yang bekerja di repositori **.notes (dotnotes)**.

---

## 0. Aturan Komunikasi & Mode Diskusi (Wajib Diikuti)

1. **Mode Diskusi (Zero Code Execution)**:
   - Setiap kali user menyebut kata **"diskusi"**, meminta penjelasan, atau mengajak merundingkan fitur/alur:
   - AI Agent **DILARANG KERAS mengubah, membuat, atau mengeksekusi kode apa pun**.
   - Fokus 100% pada penjabaran ide, konsep arsitektur, diagram alur, dan analisis teknis.
   - Pengerjaan kode **HANYA BOLEH DIMULAI** setelah user secara eksplisit memberikan instruksi persetujuan (misal: *"oke kerjakan"*, *"terapkan"*, atau *"lanjut eksekusi"*).

2. **Aturan Commit & Push Mandiri**:
   - Dilarang keras melakukan `git commit`, `git push`, atau membuat GitHub Release otomatis tanpa perintah eksplisit dari user.
   - Pengujian lokal hanya dilakukan via compile APK dan install langsung ke HP via adb debugging.

---

## 1. Ringkasan Project

**.notes (dotnotes)** adalah aplikasi catatan harian dan pengingat alarm layar penuh untuk platform Android native yang mengutamakan kecepatan, kesederhanaan, dan privasi penuh. Aplikasi berjalan secara *offline-first* (100% gratis, tanpa iklan), dengan fitur opsional sinkronisasi cloud ke Supabase via Google Sign-In.

---

## 2. Tech Stack

- **Platform & SDK**: Android Native (Min SDK 26 / Android 8.0 Oreo, Target SDK 36, Compile SDK 36, AGP 9.3.0).
- **Bahasa & Compiler**: Kotlin 2.3.21 (KSP 2.3.11, JVM Target Java 17).
- **UI Framework**: Jetpack Compose (BOM 2026.04.01) + Material Design 3 (`androidx.compose.material3`) + Material Icons Extended.
- **Navigasi**: Jetpack Navigation Compose 2.9.8 (`androidx.navigation.compose`).
- **Penyimpanan Lokal**:
  - **Database**: Room Database 2.8.4 (`androidx.room:room-runtime`, `room-ktx`, `room-compiler`).
  - **Key-Value / Preferences**: AndroidX DataStore Preferences 1.2.1.
- **Rich Text Editor**: `com.mohamedrejeb.richeditor:richeditor-compose:1.0.0`.
- **Backend & Cloud Sync (Opsional)**:
  - Supabase Kotlin SDK 3.1.1 (`supabase-kt` BOM: `auth-kt`, `postgrest-kt`).
  - Network Engine: Ktor Client OkHttp 3.1.1.
  - Serialization: `kotlinx-serialization-json:1.8.0`.
- **Autentikasi**: AndroidX Credentials 1.6.0 + Google ID 1.2.0 + Play Services Auth 21.3.0.
- **Background Tasks**: AndroidX WorkManager 2.9.1 (`androidx.work:work-runtime-ktx`).
- **Image Loading & JSON**: Coil Compose 2.7.0 (`io.coil-kt:coil-compose`), Gson 2.14.0.

---

## 3. Struktur Folder

```text
.notes/
├── app/
│   ├── build.gradle.kts                # Konfigurasi modul app, dependencies, signing configs, BuildConfig
│   ├── release.jks                     # Keystore tanda tangan build release
│   └── src/main/
│       ├── AndroidManifest.xml         # Deklarasi permission, activity, service, receiver, file provider
│       ├── java/com/dotnotes/app/
│       │   ├── DotNotesApp.kt          # Application class: inisialisasi Room DB, DataStore, Notif Channels, WorkManager
│       │   ├── MainActivity.kt         # Single Activity utama: deep links Supabase, runtime permission, compose root
│       │   ├── alarm/                  # Modul Alarm & Pengingat
│       │   │   ├── AlarmActivity.kt    # Full-screen lockscreen activity untuk alarm berdering
│       │   │   ├── AlarmPlayer.kt      # Audio looping & vibration controller
│       │   │   ├── AlarmReceiver.kt    # BroadcastReceiver penerima pemicu alarm, aksi Dismiss & Snooze
│       │   │   ├── AlarmScheduler.kt   # Penjadwalan via AlarmManager (setAlarmClock / setExactAndAllowWhileIdle)
│       │   │   ├── AlarmService.kt     # Foreground service untuk pemutaran audio alarm
│       │   │   ├── BootReceiver.kt     # Mengembalikan alarm aktif setelah perangkat reboot
│       │   │   └── ReminderHelper.kt   # Kalkulator interval pengingat (Daily, Weekly, Monthly, Yearly)
│       │   ├── data/
│       │   │   ├── local/              # NoteDatabase & NoteDao (Room Database)
│       │   │   ├── model/              # Model data entitas Note
│       │   │   ├── preferences/        # SettingsDataStore (tema, bahasa, durasi snooze, last sync)
│       │   │   └── repository/         # NoteRepository (abstraksi data lokal + pemicu cloud sync)
│       │   ├── sync/                   # Modul Sinkronisasi & Backup
│       │   │   ├── BackupManager.kt    # Ekspor/Impor file backup JSON lokal via SAF
│       │   │   └── supabase/           # Integrasi Supabase (GoogleAuthManager, SupabaseSyncManager, DTO, Client)
│       │   ├── ui/
│       │   │   ├── i18n/               # Localization.kt (AppStrings, EnglishStrings, IndonesianStrings, LocalStrings)
│       │   │   ├── navigation/         # NavGraph.kt (Routes, Compose NavHost, transisi animasi layar)
│       │   │   ├── screens/            # Layar & ViewModel Compose
│       │   │   │   ├── alarm/          # AlarmScreen
│       │   │   │   ├── editor/         # NoteEditorScreen, NoteEditorViewModel
│       │   │   │   ├── history/        # HistoryScreen (Tab pengingat aktif & riwayat selesai)
│       │   │   │   ├── notelist/       # NoteListScreen, NoteListViewModel
│       │   │   │   ├── settings/       # SettingsScreen, SettingsViewModel
│       │   │   │   └── splash/         # SplashScreen
│       │   │   └── theme/              # Theme.kt, Color.kt, NoteColorTheme.kt (8 palet warna), Type.kt
│       │   └── update/                 # Modul Auto Updater GitHub Release
│       │       ├── UpdateCheckWorker.kt# WorkManager periodic check pembaruan (tiap 12 jam)
│       │       ├── UpdateDismissReceiver.kt # Handler dismiss notif update
│       │       └── UpdateManager.kt    # Engine cek versi GitHub API, download APK, & instalasi via FileProvider
│       └── res/                        # Resources XML (icons, strings, colors, themes, file_paths, network security)
├── supabase/
│   └── schema_and_rls.sql              # Skema tabel notes PostgreSQL & kebijakan Row Level Security (RLS)
├── gradle/
│   └── libs.versions.toml              # Version Catalog Gradle
├── build.gradle.kts                    # Root build configuration
├── settings.gradle.kts                 # Plugin repository & project inclusion
└── local.properties                    # Kredensial lokal (SDK, Supabase URL/Key, Google Client ID, Keystore password)
```

---

## 4. Konvensi Kode & Pola Arsitektur

- **Arsitektur**: MVVM (Model-View-ViewModel) + Repository Pattern.
- **State Management**:
  - UI State di ViewModel diekspos sebagai Kotlin Coroutines `StateFlow` via `.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ...)`.
  - Di Composable dikonsumsi via `by viewModel.state.collectAsState()`.
- **Dependency Injection**:
  - Manual Service Locator ringan via `DotNotesApp.instance` (`database`, `repository`, `settingsDataStore`).
  - ViewModels diinstansiasi menggunakan `ViewModelProvider.Factory` kustom yang menerima dependencies di constructor.
- **Multi-Bahasa (i18n)**:
  - Wajib menggunakan `LocalStrings.current`.
  - Setiap teks baru **HARUS** didaftarkan di `Localization.kt` pada `AppStrings`, `EnglishStrings`, dan `IndonesianStrings`.
- **Penghapusan & Sinkronisasi (Soft Delete)**:
  - Penghapusan catatan lokal menandai `isDeleted = true` terlebih dahulu (`softDeleteNote`), agar perubahan terpropagasi ke Supabase.
  - Setelah sinkronisasi cloud berhasil atau saat konfirmasi permanen, data baru di-hard delete (`deleteNotesPermanently`).
- **Editor Rich Text**:
  - Menggunakan `RichTextState` untuk formatting HTML tebal/miring/daftar poin.
  - Teks preview daftar catatan diproses via `Note.getPreviewText()` (pembersihan tag HTML).
- **Penamaan & Format**:
  - PascalCase: Composable functions, Activities, ViewModels, Classes, Enums.
  - camelCase: Variabel, properti, fungsi, arguments.
  - UPPER_SNAKE_CASE: Konstanta (Routes, Intent Extras/Actions, Notification Channel IDs, SharedPreferences Keys).

---

## 5. Cara Run & Setup Project

### Persiapan Lingkungan:
- JDK 17 atau Zulu JDK 21.
- Android SDK Platform 36 & Build-Tools.

### Konfigurasi `local.properties` (Root Folder):
```properties
sdk.dir=C:\\Users\\<USER>\\AppData\\Local\\Android\\Sdk
SUPABASE_URL=https://<project-ref>.supabase.co
SUPABASE_ANON_KEY=<anon-key>
GOOGLE_WEB_CLIENT_ID=<google-client-id>.apps.googleusercontent.com
RELEASE_KEYSTORE_PASSWORD=<password>
RELEASE_KEY_PASSWORD=<password>
RELEASE_KEY_ALIAS=dotnotes
```

### Perintah Build & Run:

1. **Build APK Debug**:
   ```powershell
   .\gradlew.bat assembleDebug
   ```
2. **Build APK Release (Signed)**:
   ```powershell
   $env:JAVA_HOME = "C:\Program Files\Zulu\zulu-21"
   .\gradlew.bat assembleRelease --no-daemon
   ```
3. **Install Langsung ke Perangkat (via ADB)**:
   ```powershell
   adb install -r app\build\outputs\apk\debug\app-debug.apk
   # atau untuk release:
   adb install -r app\build\outputs\apk\release\app-release.apk
   ```

---

## 6. Hal-Hal Penting & Gotcha (Perhatian Khusus)

1. **Room Database Migrations**:
   - Versi schema Room saat ini: **v3**.
   - Setiap modifikasi tabel `notes` **WAJIB** menyertakan migration eksplisit (`MIGRATION_X_Y`) di `NoteDatabase.kt` dan didaftarkan di `DotNotesApp.kt`. Dilarang menggunakan `fallbackToDestructiveMigration()` pada rilis produksi agar data user tidak terhapus.
2. **Exact Alarm & Full-Screen Intent Permissions (Android 12–14+)**:
   - Android 12+ (API 31+) membutuhkan pengecekan `AlarmManager.canScheduleExactAlarms()` dan prompt intent `ACTION_REQUEST_SCHEDULE_EXACT_ALARM`.
   - Android 14+ (API 34+) memperketat background activity launch; `AlarmReceiver` menggunakan `ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED` untuk membuka `AlarmActivity` layar penuh.
3. **In-App Updater & Android FileProvider**:
   - Download APK update disimpan di cache aplikasi dan dibuka via `androidx.core.content.FileProvider` dengan authority `${applicationId}.fileprovider`.
   - Membutuhkan izin `REQUEST_INSTALL_PACKAGES` (user dialihkan ke settings jika belum diizinkan).
4. **Supabase Row Level Security (RLS)**:
   - Supabase sync bergantung pada `user_id = auth.uid()`. Database SQL wajib mengaktifkan RLS dari `supabase/schema_and_rls.sql` untuk mencegah kebocoran data antar user.
   - Resolusi konflik sinkronisasi mengandalkan field timestamp milidetik `updatedAt` (*last write wins*).

---

## 7. Logika Penentuan Pesan Commit & Versi Otomatis

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

## 8. SOP Rilis & Pembaruan Versi (Langkah Demi Langkah)

Setiap kali user meminta **"commit"**, **"rilis"**, atau **"update versi"**, jalankan urutan ini:

### Langkah 1: Rumuskan Commit & Update Versi di File Konfigurasi
1. Tentukan pesan commit sesuai instruksi user (misal: `fix: adjust reminder time display and floating notification button`).
2. Evaluasi tipe commit (`feat` / `fix` / `style` / dsb.) untuk menentukan versi baru (`MAJOR`, `MINOR`, atau `PATCH`).
3. Buka `app/build.gradle.kts`:
   - `versionCode`: Tambah `+1` (integer) dari nilai sebelumnya.
   - `versionName`: Perbarui ke versi baru (contoh: `"1.21.5"`).
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

## 9. Aturan Desain & Git Hygiene
1. **UI Bersih & Seamless**: Hindari border kotak tebal pada teks input; utamakan tipografi modern dan *borderless editor*.
2. **Penanganan Notifikasi**: Selalu sertakan tombol aksi langsung seperti *"Tandai Selesai"* dan *"Tunda"* pada notifikasi pengingat & alarm.
3. **Git Hygiene**: Jangan pernah menambahkan file `.apk` ke dalam commit Git biasa (file `.apk` hanya diunggah ke GitHub Releases via `gh`).
