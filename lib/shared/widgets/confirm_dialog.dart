import 'package:flutter/material.dart';
import 'package:dotnotes/app/theme.dart';

class ConfirmDialog extends StatelessWidget {
  final String title;
  final String message;
  final String confirmText;
  final String cancelText;

  const ConfirmDialog({
    super.key,
    required this.title,
    required this.message,
    this.confirmText = 'Delete',
    this.cancelText = 'Cancel',
  });

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: Text(title),
      content: Text(message),
      actions: [
        TextButton(
          onPressed: () {
            Navigator.pop(context, false);
          },
          child: Text(
            cancelText,
            style: const TextStyle(color: AppColors.textMuted),
          ),
        ),
        TextButton(
          onPressed: () {
            Navigator.pop(context, true);
          },
          style: TextButton.styleFrom(
            foregroundColor: AppColors.error,
          ),
          child: Text(confirmText),
        ),
      ],
    );
  }
}
