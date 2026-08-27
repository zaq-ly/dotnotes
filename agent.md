# SOP Commit, Semantic Versioning & Rilis .notes (dotnotes)

Setiap kali user meminta untuk melakukan **commit** atau **rilis versi baru** (contoh: "commit", "rilis", "update ke versi baru"), jalankan aturan dan langkah-langkah wajib berikut secara berurutan:

---

## 1. Aturan Semantic Versioning (SemVer: `MAJOR.MINOR.PATCH`)

AI Agent **wajib menganalisis cakupan perubahan** yang baru dikerjakan untuk menentukan penomoran versi yang tepat:

| Jenis Rilis | Format | Kapan Digunakan | Contoh Perubahan |
| :--- | :--- | :--- | :--- |
| **MAJOR** | `X.0.0` | Perombakan total aplikasi, perubahan arsitektur besar, atau *breaking changes* struktur database. | Rebranding total, pergantian framework UI/arsitektur database. |
| **MINOR** | `1.X.0` | **Fitur fungsional baru** ditambahkan (kompatibel ke belakang). Reset `PATCH` ke `0`. | Menambahkan fitur Export/Import JSON, In-App Updater, Multi-bahasa, Dark Mode, Pencarian. |
| **PATCH** | `1.0.X` | **Bug fix, polesan UI/UX, penyesuaian tata letak, teks, atau refactoring** tanpa fitur baru. | Memperbaiki format jam alarm, memindah posisi tombol pengingat, merapikan border teks. |

---

## 2. Langkah-Langkah Wajib Saat Rilis

### Langkah A: Update Versi di Gradle & Tampilan UI
1. Buka `app/build.gradle.kts` dan perbarui `defaultConfig`:
   - `versionCode`: Naikkan `+1` (integer) dari nilai sebelumnya.
   - `versionName`: Ubah sesuai aturan SemVer di atas (contoh: `"1.1.0"` jika ada fitur baru, atau `"1.0.11"` jika patch/fix).
2. Buka `app/src/main/java/com/dotnotes/app/ui/screens/settings/SettingsScreen.kt`:
   - Update teks `About` agar memuat string versi terbaru (contoh: `"dotnotes v1.1.0"`).
   - Update argumen fungsi `viewModel.checkForUpdate("[VERSION_NAME]")` agar mencocokkan versi saat ini.

### Langkah B: Build APK
Jalankan command build Gradle:
```powershell
$env:JAVA_HOME = "C:\Program Files\Zulu\zulu-21"
.\gradlew.bat assembleDebug --no-daemon
```

### Langkah C: Rename File APK
Setelah build berhasil, copy file APK ke root directory dengan format nama rilis:
```powershell
Copy-Item "app\build\outputs\apk\debug\app-debug.apk" "dotnotes-v.[VERSION_NAME].apk"
```
*(Ganti `[VERSION_NAME]` dengan versi yang sesuai, misal: `dotnotes-v.1.1.0.apk`)*

### Langkah D: Git Commit & Push
Commit semua perubahan kode ke GitHub:
```powershell
git add .
git commit -m "Release v[VERSION_NAME] - [Ringkasan Perubahan]"
git push
```

### Langkah E: Buat GitHub Release + Upload APK
Gunakan GitHub CLI (`gh`) untuk membuat rilis baru sekaligus melampirkan file APK:
```powershell
gh release create v[VERSION_NAME] dotnotes-v.[VERSION_NAME].apk --title "Release v[VERSION_NAME]" --notes "Update to version [VERSION_NAME] - [Daftar Perubahan]"
```

### Langkah F: Cleanup
Hapus file APK lokal yang sudah di-upload agar tidak menumpuk di folder root (karena APK tidak boleh masuk git commit):
```powershell
Remove-Item "dotnotes-v.[VERSION_NAME].apk"
```

---

## Catatan Penting untuk AI Agent
- Selalu patuhi standar SemVer di atas. Jangan menaikkan patch terus-menerus jika ada penambahan fitur baru (naikkan minor).
- Jangan lakukan `git add` pada file APK, pastikan file APK hanya di-upload ke GitHub Release melalui command `gh release create`.
