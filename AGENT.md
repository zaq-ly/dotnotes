# Agent: Release Manager

Automate versioning, commit, push, tag, and GitHub release with APK artifact.

## Trigger

When user says: "commit", "push", "release", "update version"

## Workflow

1. **Check changes**
   ```bash
   git status
   git diff --stat
   git log --oneline HEAD...origin/master
   ```

2. **Auto-analyze changes**
   - Parse file changes:
     - `lib/features/` → feature changes
     - `lib/core/` → core/infrastructure changes
     - `*.md`, `README` → docs only
     - `pubspec.yaml` → dependency updates
   - Classify commits since last tag:
     - Added files → "feat"
     - Bug fixes in code → "fix"
     - Refactor → "refactor"
     - Docs → "docs"
     - Config/build → "chore"

3. **Version bump**
   - Read current version from `pubspec.yaml`
   - Auto-decide or ask user:
     - Breaking changes → major (x.0.0)
     - New features → minor (0.x.0)
     - Bug fixes only → patch (0.0.x)
   - Update `pubspec.yaml`: version + versionCode

4. **Build APK**
   ```bash
   flutter clean
   flutter pub get
   flutter pub run build_runner build --delete-conflicting-outputs
   flutter build apk --release --split-per-abi
   ```
   - APK: `build/app/outputs/flutter-apk/app-armeabi-v7a-release.apk`

5. **Auto-generate release notes**
   Format:
   ```
   ## What's New
   
   ### Features
   - Added reminder snooze (10min/1hr/custom)
   - Notification priority modes
   
   ### Bug Fixes
   - Fixed alarm not rescheduling after device reboot
   - Fixed notification permission on Android 13+
   
   ### Improvements
   - Better date picker UX
   - Reduced APK size by 2MB
   ```

6. **Git commit**
   - Auto-generate commit message from changes
   - `git add .`
   - `git commit -m "<auto-message>"`

7. **Git tag**
   - `git tag -a v<version> -m "<release notes>"`

8. **Push**
   ```bash
   git push origin master
   git push origin v<version>
   ```

9. **GitHub Release**
   ```bash
   gh release create v<version> \
     build/app/outputs/flutter-apk/app-armeabi-v7a-release.apk \
     --title "v<version>" \
     --notes "<release notes>"
   ```

## Example Flow

Current state (v0.0.1):
- Added Phase 1: Notes (CRUD, search, pin, archive, categories)
- Added Phase 2: Reminders + Alarm
- Added AGENT.md

User: "release new version"

Agent output:
```
Changes detected:
- lib/features/reminders/ (new files: reminder.dart, provider, screens)
- lib/core/services/ (new files: notification_service.dart, alarm_service.dart)
- AGENT.md (new file)
- README.md (updated)

Version bump: v0.0.1 → v0.0.2 (patch)

Release notes:
## v0.0.2
- Added: Reminder scheduling with date/time
- Added: Alarm notifications (offline capable)
- Added: Priority modes (Low/Normal/High/Urgent)
- Added: Repeat options (Once/Daily/Weekly/Monthly)
- Added: Snooze functionality
- Added: AGENT.md for automated release workflow
```

Buat release: `v0.0.2` dengan APK attached

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
