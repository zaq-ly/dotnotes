# Agent: Release Manager

Automate versioning, commit, push, tag, and GitHub release with APK artifact.

## Trigger

When user says: "commit", "push", "release", "update version"

## Quick Release (1 Command)

```bash
# Otomatis: patch bump + generate notes + build APK + git commit/tag + push + GitHub release
dart run tool/release.dart patch

# Atau tentukan bump type:
dart run tool/release.dart minor
dart run tool/release.dart major

# Preview tanpa eksekusi:
dart run tool/release.dart patch --dry-run
```

## Step-by-Step Manual Workflow

1. **Check changes & analyze**
   ```bash
   git status
   git diff --stat
   git log --oneline HEAD...origin/master
   ```

2. **Version bump in `pubspec.yaml`**
   - Major (`x.0.0+n`): Breaking changes
   - Minor (`0.x.0+n`): New features
   - Patch (`0.0.x+n`): Bug fixes

3. **Build APK**
   ```bash
   flutter clean
   flutter pub get
   dart run build_runner build --delete-conflicting-outputs
   flutter build apk --release --split-per-abi
   ```
   - APKs: `build/app/outputs/flutter-apk/*-release.apk`

4. **Git commit & tag**
   ```bash
   git add .
   git commit -m "chore(release): v<version>"
   git tag -a v<version> -m "<release notes>"
   git push origin master --tags
   ```

5. **GitHub Release**
   ```bash
   gh release create v<version> \
     build/app/outputs/flutter-apk/*-release.apk \
     --title "v<version>" \
     --notes "<release notes>"
   ```

## CI/CD Workflow

File `.github/workflows/release.yml` otomatis membangun APK dan menerbitkan GitHub Release setiap kali tag `v*` di-push atau di-trigger manual lewat Actions tab.
