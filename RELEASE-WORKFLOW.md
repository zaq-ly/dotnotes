# Release Workflow — .notes

**Referensi:** `ARCHITECTURE.md` (lihat bagian 8. Versioning)
**File aktual:** `.github/workflows/release.yml`

---

## 1. Tujuan

Otomatisasi bump versi, build APK, dan publish GitHub Release setiap ada commit masuk ke `main` — supaya versi di `pubspec.yaml`, tag GitHub, dan APK release selalu sinkron dari satu sumber, tanpa perlu tag manual.

---

## 2. Trigger

- Jalan otomatis tiap **push ke branch `main`**
- Diabaikan kalau yang berubah cuma file `.md` atau isi folder `.github/`
- Skip run kalau pesan commit mengandung `[skip ci]` (mencegah loop tak berujung, karena workflow ini sendiri commit balik ke `main`)

---

## 3. Aturan Bump Versi (dari pesan commit)

| Format pesan commit | Bump |
|---|---|
| `feat!: ...` atau mengandung `BREAKING CHANGE` | major |
| `feat: ...` | minor |
| Selain itu (mis. `fix:`, `chore:`, dll) | patch |

Build number (`+N` di `pubspec.yaml`) selalu naik +1 tiap run, terlepas dari jenis bump.

---

## 4. Alur Kerja

1. Checkout repo (full history)
2. Setup Flutter
3. Baca pesan commit terakhir → tentukan jenis bump (major/minor/patch)
4. Parse versi saat ini dari `pubspec.yaml`, hitung versi baru
5. Update `pubspec.yaml`, commit balik ke `main` dengan tag `[skip ci]`
6. `flutter pub get` → `flutter build apk --release`
7. Buat git tag baru (`vX.Y.Z`)
8. Publish GitHub Release dengan APK terlampir + release notes otomatis

---

## 5. File Workflow

```yaml
name: Auto Release

on:
  push:
    branches: [main]
    paths-ignore:
      - '**.md'
      - '.github/**'

permissions:
  contents: write

jobs:
  release:
    # Cegah infinite loop: skip run yang dipicu oleh commit bump version dari workflow ini sendiri
    if: "!contains(github.event.head_commit.message, '[skip ci]')"
    runs-on: ubuntu-latest

    steps:
      - name: Checkout
        uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - name: Setup Flutter
        uses: subosito/flutter-action@v2
        with:
          flutter-version: '3.24.0'
          channel: stable

      - name: Tentukan jenis bump dari pesan commit
        id: bump
        run: |
          MSG="${{ github.event.head_commit.message }}"
          if echo "$MSG" | grep -qE "^feat(\(.+\))?!:|BREAKING CHANGE"; then
            echo "type=major" >> "$GITHUB_OUTPUT"
          elif echo "$MSG" | grep -qE "^feat(\(.+\))?:"; then
            echo "type=minor" >> "$GITHUB_OUTPUT"
          else
            echo "type=patch" >> "$GITHUB_OUTPUT"
          fi

      - name: Bump versi di pubspec.yaml
        id: version
        run: |
          CURRENT=$(grep '^version:' pubspec.yaml | sed 's/version: //')
          VERSION_NAME=$(echo "$CURRENT" | cut -d'+' -f1)
          BUILD_NUMBER=$(echo "$CURRENT" | cut -d'+' -f2)
          MAJOR=$(echo "$VERSION_NAME" | cut -d. -f1)
          MINOR=$(echo "$VERSION_NAME" | cut -d. -f2)
          PATCH=$(echo "$VERSION_NAME" | cut -d. -f3)

          case "${{ steps.bump.outputs.type }}" in
            major) MAJOR=$((MAJOR+1)); MINOR=0; PATCH=0 ;;
            minor) MINOR=$((MINOR+1)); PATCH=0 ;;
            patch) PATCH=$((PATCH+1)) ;;
          esac

          NEW_BUILD=$((BUILD_NUMBER+1))
          NEW_VERSION_NAME="$MAJOR.$MINOR.$PATCH"
          NEW_FULL_VERSION="$NEW_VERSION_NAME+$NEW_BUILD"

          sed -i "s/^version: .*/version: $NEW_FULL_VERSION/" pubspec.yaml

          echo "version_name=$NEW_VERSION_NAME" >> "$GITHUB_OUTPUT"
          echo "full_version=$NEW_FULL_VERSION" >> "$GITHUB_OUTPUT"

      - name: Commit bump version
        run: |
          git config user.name "github-actions[bot]"
          git config user.email "github-actions[bot]@users.noreply.github.com"
          git add pubspec.yaml
          git commit -m "chore: bump version to ${{ steps.version.outputs.full_version }} [skip ci]"
          git push

      - name: Install dependencies
        run: flutter pub get

      - name: Build APK release
        run: flutter build apk --release

      - name: Buat tag
        run: |
          git tag v${{ steps.version.outputs.version_name }}
          git push origin v${{ steps.version.outputs.version_name }}

      - name: Buat GitHub Release
        uses: softprops/action-gh-release@v2
        with:
          tag_name: v${{ steps.version.outputs.version_name }}
          name: v${{ steps.version.outputs.version_name }}
          files: build/app/outputs/flutter-apk/app-release.apk
          generate_release_notes: true
```

---

## 6. Catatan untuk AI Coding Agent

- Jangan ubah struktur `pubspec.yaml` di luar baris `version:` lewat workflow ini.
- Kalau butuh secret tambahan (mis. signing key APK), tambahkan lewat GitHub Secrets, jangan hardcode di YAML.
- `GITHUB_TOKEN` bawaan Actions sudah cukup untuk push commit & buat release selama permission `contents: write` di-set (sudah ada di atas) — tidak perlu Personal Access Token tambahan kecuali branch protection di `main` mewajibkannya.
