import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:google_sign_in/google_sign_in.dart';
import '../../../data/remote/google/google_auth_service.dart';

class AuthNotifier extends StateNotifier<GoogleSignInAccount?> {
  final GoogleAuthService _authService;

  AuthNotifier(this._authService) : super(_authService.currentUser) {
    _authService.onCurrentUserChanged.listen((account) {
      state = account;
    });
    _authService.signInSilently();
  }

  Future<GoogleSignInAccount?> signIn() async {
    try {
      final account = await _authService.signIn();
      state = account;
      return account;
    } catch (_) {
      return null;
    }
  }

  Future<void> signOut() async {
    try {
      await _authService.signOut();
      state = null;
    } catch (_) {}
  }
}

final authNotifierProvider =
    StateNotifierProvider<AuthNotifier, GoogleSignInAccount?>((ref) {
  final authService = ref.watch(googleAuthServiceProvider);
  return AuthNotifier(authService);
});
