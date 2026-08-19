import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:dotnotes/app/app.dart';
import 'package:dotnotes/core/database/hive_service.dart';
import 'package:dotnotes/core/services/notification_service.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  
  await HiveService.init();
  await NotificationService().init();
  
  runApp(
    const ProviderScope(
      child: App(),
    ),
  );
}
