import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:google_sign_in/google_sign_in.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:dotnotes/data/remote/google/google_auth_service.dart';
import 'package:dotnotes/features/auth/application/auth_notifier.dart';
import 'package:dotnotes/features/settings/application/settings_notifier.dart';
import 'package:dotnotes/features/settings/presentation/settings_screen.dart';
import 'package:dotnotes/l10n/app_localizations.dart';

class FakeGoogleSignInAccount implements GoogleSignInAccount {
  @override
  final String displayName = 'Tester User';
  @override
  final String email = 'tester@gmail.com';
  @override
  final String id = '123456';
  @override
  final String? photoUrl = null;
  @override
  final String? serverAuthCode = null;

  @override
  Future<GoogleSignInAuthentication> get authentication async =>
      throw UnimplementedError();

  @override
  Future<Map<String, String>> get authHeaders async => {};

  @override
  Future<void> clearAuthCache() async {}
}

void main() {
  testWidgets('SettingsScreen shows Google Drive Sync section when user is signed in',
      (WidgetTester tester) async {
    SharedPreferences.setMockInitialValues({});
    final sharedPreferences = await SharedPreferences.getInstance();
    final fakeUser = FakeGoogleSignInAccount();

    await tester.pumpWidget(
      ProviderScope(
        overrides: [
          sharedPreferencesProvider.overrideWithValue(sharedPreferences),
          authNotifierProvider.overrideWith((ref) {
            final notifier = AuthNotifier(ref.watch(googleAuthServiceProvider));
            // ignore: invalid_use_of_protected_member
            notifier.state = fakeUser;
            return notifier;
          }),
        ],
        child: const MaterialApp(
          localizationsDelegates: AppLocalizations.localizationsDelegates,
          supportedLocales: AppLocalizations.supportedLocales,
          home: SettingsScreen(),
        ),
      ),
    );
    await tester.pumpAndSettle();

    // Verify user profile
    expect(find.text('Tester User'), findsOneWidget);
    expect(find.text('tester@gmail.com'), findsOneWidget);
    expect(find.text('Sign Out'), findsOneWidget);

    // Verify Google Drive Sync section
    expect(find.text('Google Drive Sync'), findsOneWidget);
    expect(find.text('Sync Now'), findsOneWidget);
    expect(find.byIcon(Icons.sync), findsOneWidget);
  });
}
