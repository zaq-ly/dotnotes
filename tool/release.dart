// ignore_for_file: avoid_print
import 'dart:io';

void main(List<String> args) async {
  final bumpType = args.isNotEmpty && ['major', 'minor', 'patch'].contains(args[0])
      ? args[0]
      : 'patch';
  final isDryRun = args.contains('--dry-run');
  final skipBuild = args.contains('--skip-build');

  print('=== Release Manager (.notes) ===');
  print('Bump type: $bumpType | Dry run: $isDryRun | Skip build: $skipBuild\n');

  // 1. Read pubspec.yaml
  final pubspecFile = File('pubspec.yaml');
  if (!pubspecFile.existsSync()) {
    print('Error: pubspec.yaml not found.');
    exit(1);
  }

  final pubspecContent = pubspecFile.readAsStringSync();
  final versionRegex = RegExp(r'^version:\s*([0-9]+)\.([0-9]+)\.([0-9]+)(?:\+([0-9]+))?', multiLine: true);
  final match = versionRegex.firstMatch(pubspecContent);

  if (match == null) {
    print('Error: Could not parse version in pubspec.yaml');
    exit(1);
  }

  int major = int.parse(match.group(1)!);
  int minor = int.parse(match.group(2)!);
  int patch = int.parse(match.group(3)!);
  int build = int.parse(match.group(4) ?? '1');

  switch (bumpType) {
    case 'major':
      major += 1;
      minor = 0;
      patch = 0;
      break;
    case 'minor':
      minor += 1;
      patch = 0;
      break;
    case 'patch':
    default:
      patch += 1;
      break;
  }
  build += 1;

  final newVersion = '$major.$minor.$patch';
  final newVersionFull = '$newVersion+$build';
  final tag = 'v$newVersion';

  print('Current: ${match.group(0)}');
  print('New:     version: $newVersionFull (Tag: $tag)\n');

  // 2. Fetch latest tag & generate release notes from git log
  final lastTagResult = await Process.run('git', ['describe', '--tags', '--abbrev=0'], runInShell: true);
  final lastTag = lastTagResult.exitCode == 0 ? lastTagResult.stdout.toString().trim() : '';

  final logArgs = lastTag.isNotEmpty
      ? ['log', '$lastTag..HEAD', '--pretty=format:%s']
      : ['log', '-n', '15', '--pretty=format:%s'];

  final gitLogResult = await Process.run('git', logArgs, runInShell: true);
  final commitLines = gitLogResult.stdout.toString().split('\n').where((l) => l.trim().isNotEmpty).toList();

  final featList = <String>[];
  final fixList = <String>[];
  final otherList = <String>[];

  for (final line in commitLines) {
    if (line.startsWith('feat:') || line.startsWith('feat(')) {
      featList.add(line.replaceFirst(RegExp(r'^feat(\([^)]+\))?:\s*'), ''));
    } else if (line.startsWith('fix:') || line.startsWith('fix(')) {
      fixList.add(line.replaceFirst(RegExp(r'^fix(\([^)]+\))?:\s*'), ''));
    } else {
      otherList.add(line);
    }
  }

  final notesBuffer = StringBuffer();
  notesBuffer.writeln('## What\'s New in $tag\n');

  if (featList.isNotEmpty) {
    notesBuffer.writeln('### Features');
    for (final f in featList) {
      notesBuffer.writeln('- $f');
    }
    notesBuffer.writeln();
  }

  if (fixList.isNotEmpty) {
    notesBuffer.writeln('### Bug Fixes');
    for (final f in fixList) {
      notesBuffer.writeln('- $f');
    }
    notesBuffer.writeln();
  }

  if (featList.isEmpty && fixList.isEmpty) {
    notesBuffer.writeln('### Updates');
    for (final o in otherList.take(5)) {
      notesBuffer.writeln('- $o');
    }
    if (otherList.isEmpty) {
      notesBuffer.writeln('- Maintenance and stability improvements.');
    }
    notesBuffer.writeln();
  }

  notesBuffer.writeln('---');
  notesBuffer.writeln('### Panduan Memilih File APK Berdasarkan Seri Android:');
  notesBuffer.writeln('| File APK | Versi / Seri Android | Jenis Perangkat | Keterangan |');
  notesBuffer.writeln('| :--- | :--- | :--- | :--- |');
  notesBuffer.writeln('| **`dotnotes-$tag-arm64-v8a.apk`** | **Android 10, 11, 12, 13, 14, 15+** | **HP Android Modern (64-bit)** | **Pilihan Utama (Wajib untuk HP sekarang)** |');
  notesBuffer.writeln('| **`dotnotes-$tag-armeabi-v7a.apk`** | **Android 5.0 s/d Android 9.0** | HP Android Jadul (32-bit) | Khusus HP model lama |');
  notesBuffer.writeln('| **`dotnotes-$tag-x86_64.apk`** | **Android 7.0 s/d Android 14** (di PC) | Emulator PC / Laptop | Khusus emulator Android Studio / BlueStacks / LDPlayer |');

  final releaseNotes = notesBuffer.toString().trim();
  print('--- Release Notes ---\n$releaseNotes\n---------------------');

  if (isDryRun) {
    print('Dry run complete. No changes made.');
    return;
  }

  // 3. Update pubspec.yaml
  final updatedPubspec = pubspecContent.replaceFirst(
    versionRegex,
    'version: $newVersionFull',
  );
  pubspecFile.writeAsStringSync(updatedPubspec);
  print('Updated pubspec.yaml -> version: $newVersionFull');

  // 4. Build APKs
  if (!skipBuild) {
    print('\nCleaning and building APKs...');
    await _runCmd('flutter', ['clean']);
    await _runCmd('flutter', ['pub', 'get']);
    await _runCmd('flutter', ['build', 'apk', '--release', '--split-per-abi']);
  }

  // 5. Commit & Tag
  print('\nCreating Git commit and tag...');
  await _runCmd('git', ['add', '.']);
  await _runCmd('git', ['commit', '-m', 'chore(release): $tag']);
  await _runCmd('git', ['tag', '-a', tag, '-m', releaseNotes]);

  // 6. Push
  print('\nPushing to remote origin...');
  await _runCmd('git', ['push', 'origin', 'master', '--tags']);

  // 7. GitHub Release via gh CLI
  print('\nPublishing GitHub Release...');
  final apkDir = Directory('build/app/outputs/flutter-apk');
  final uploadFiles = <String>[];
  if (apkDir.existsSync()) {
    for (final file in apkDir.listSync().whereType<File>()) {
      final name = file.uri.pathSegments.last;
      if (name.startsWith('app-') && name.endsWith('-release.apk')) {
        final abi = name.replaceFirst('app-', '').replaceFirst('-release.apk', '');
        final targetPath = '${apkDir.path}/dotnotes-$tag-$abi.apk';
        file.copySync(targetPath);
        uploadFiles.add(targetPath);
      }
    }
  }

  final ghArgs = [
    'release',
    'create',
    tag,
    ...uploadFiles,
    '--title',
    tag,
    '--notes',
    releaseNotes,
  ];

  final ghRes = await Process.run('gh', ghArgs, runInShell: true);
  if (ghRes.exitCode == 0) {
    print('GitHub Release created successfully: ${ghRes.stdout.toString().trim()}');
  } else {
    print('GitHub CLI release notice: ${ghRes.stderr.toString().trim()}');
  }

  print('\nRelease $tag finished successfully!');
}

Future<void> _runCmd(String cmd, List<String> args) async {
  final proc = await Process.start(cmd, args, mode: ProcessStartMode.inheritStdio, runInShell: true);
  final code = await proc.exitCode;
  if (code != 0) {
    print('Command failed ($cmd ${args.join(' ')}): exit code $code');
    exit(code);
  }
}
