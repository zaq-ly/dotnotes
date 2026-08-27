# SOP Commit & Rilis .notes (dotnotes)

Setiap kali user meminta untuk melakukan **commit** atau **rilis versi baru** (contoh: "commit ke v1.0.1"), jalankan langkah-langkah wajib berikut secara berurutan:

## 1. Update Versi di Gradle
Buka file `app/build.gradle.kts` dan update blok `defaultConfig`:
- Naikkan `versionCode` (tambah 1 dari angka sebelumnya).
- Ubah `versionName` sesuai dengan versi yang diminta user (contoh: `"1.0.1"`).

## 2. Build APK
Jalankan command build Gradle:
```powershell
$env:JAVA_HOME = "C:\Program Files\Zulu\zulu-21"
.\gradlew.bat assembleDebug --no-daemon
```

## 3. Rename File APK
Setelah build berhasil, copy dan rename file APK yang dihasilkan agar namanya memuat versi terbaru:
```powershell
Copy-Item "app\build\outputs\apk\debug\app-debug.apk" "dotnotes-v.[VERSION_NAME].apk"
```
*(Ganti `[VERSION_NAME]` dengan `versionName` yang sesuai, misal: `dotnotes-v.1.0.1.apk`)*

## 4. Git Commit & Push
Commit semua perubahan kode ke GitHub:
```powershell
git add .
git commit -m "Release v[VERSION_NAME]"
git push
```

## 5. Buat GitHub Release + Upload APK
Gunakan GitHub CLI (`gh`) untuk membuat rilis baru sekaligus melampirkan file APK-nya ke halaman rilis GitHub:
```powershell
gh release create v[VERSION_NAME] dotnotes-v.[VERSION_NAME].apk --title "Release v[VERSION_NAME]" --notes "Update to version [VERSION_NAME]"
```

## 6. Cleanup
Hapus file APK lokal yang sudah di-upload agar tidak menumpuk di folder root (karena APK tidak boleh masuk git commit):
```powershell
Remove-Item "dotnotes-v.[VERSION_NAME].apk"
```

---
**Catatan untuk AI Agent:**
- Selalu patuhi SOP ini setiap kali user bilang "commit" atau "release".
- Jangan lakukan `git add` pada file APK, pastikan `.gitignore` sudah mengatur hal ini, file APK hanya di-upload ke GitHub Release melalui command `gh release create`.
