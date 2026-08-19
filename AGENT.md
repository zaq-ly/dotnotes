# Agent: Release Manager

Automate versioning, commit, push, tag, and GitHub release with APK artifact.

## Trigger

When user says: "commit", "push", "release", "update version"

## Workflow

1. **Version bump**
   - Read current version from `pubspec.yaml`
   - Ask user: patch (0.0.x), minor (0.x.0), or major (x.0.0)?
   - Update `pubspec.yaml` version + versionCode

2. **Build APK**
   ```bash
   flutter clean
   flutter pub get
   flutter pub run build_runner build --delete-conflicting-outputs
   flutter build apk --release --split-per-abi
   ```
   - APK at: `build/app/outputs/flutter-apk/app-armeabi-v7a-release.apk`

3. **Git commit**
   - `git status` → check changes
   - Ask user for commit message (or auto-generate from changes)
   - `git add .`
   - `git commit -m "<message>"`

4. **Git tag**
   - Tag format: `v<version>` (e.g., `v0.0.2`)
   - Ask user for release notes (or auto-generate: "Bug fixes", "New features", etc.)
   - `git tag -a v<version> -m "<release notes>"`

5. **Push**
   ```bash
   git push origin master
   git push origin v<version>
   ```

6. **GitHub Release**
   - Use `gh` CLI to create release
   ```bash
   gh release create v<version> \
     build/app/outputs/flutter-apk/app-armeabi-v7a-release.apk \
     --title "Release v<version>" \
     --notes "<release notes>"
   ```

## Example Flow

User: "commit and release with bug fixes"

Agent:
1. Current version: `0.0.1`
2. Bump to: `0.0.2` (patch)
3. Update `pubspec.yaml`
4. Build APK
5. Commit: "fix: bug fixes for alarm service"
6. Tag: `v0.0.2` with notes "Bug fixes: alarm service stability"
7. Push to GitHub
8. Create release with APK attached

## Requirements

- `gh` CLI installed and authenticated: `gh auth login`
- Flutter SDK installed
- Git configured with remote `origin`

## Notes

- APK split by ABI (armeabi-v7a for most Android devices)
- Release notes can be:
  - User-provided
  - Auto-generated from git diff summary
  - Template: "feat: ...", "fix: ...", "chore: ..."
