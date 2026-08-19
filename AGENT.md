# Agent: Release Manager

Automate versioning, commit, push, tag, and GitHub release with APK artifact.

## Trigger

When user says: "commit", "push", "release", "update version"

## Quick Release (1 Command)

```bash
# Rilis otomatis dengan deskripsi kustom:
dart run tool/release.dart patch -m "Perbaikan alarm, update logo baru, dan optimasi performa"

# Atau pilihan bump lainnya:
dart run tool/release.dart minor -m "Fitur Google Calendar & Gmail integration"
dart run tool/release.dart major -m "Rilis besar v2.0.0"

# Preview changelog tanpa merilis:
dart run tool/release.dart patch -m "Testing changelog" --dry-run
```

## Apa yang Dilakukan Otomatis:
1. **Version bump**: Memperbarui versi & build number di `pubspec.yaml`.
2. **Release Notes Generator**: Menyusun changelog rapi dari deskripsi pesan `-m`, riwayat commit `feat:` / `fix:`, dan file yang diubah.
3. **Build APK**: Menghasilkan split APK (`arm64-v8a`, `armeabi-v7a`, `x86_64`) dan menamainya `dotnotes-v<version>-<abi>.apk`.
4. **Git Commit & Tag**: `git add .`, commit dengan pesan rilis, dan buat tag berversi.
5. **Git Push**: Push kode dan tag ke `origin master`.
6. **GitHub Release**: Mengunggah file APK ke GitHub Release lengkap dengan deskripsi dan tabel panduan seri Android.

## CI/CD Workflow

File `.github/workflows/release.yml` otomatis membangun APK dan menerbitkan GitHub Release setiap kali tag `v*` di-push ke GitHub.
