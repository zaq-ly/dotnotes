import 'package:flutter/material.dart';

extension DateTimeX on DateTime {
  String toReadableString() {
    final now = DateTime.now();
    final diff = now.difference(this);

    if (diff.inDays == 0) {
      if (diff.inHours == 0) {
        if (diff.inMinutes == 0) {
          return 'Now';
        }
        return '${diff.inMinutes}m';
      }
      return '${diff.inHours}h';
    } else if (diff.inDays < 7) {
      return '${diff.inDays}d';
    } else {
      return '$day/$month/$year';
    }
  }

  String toTimeString() {
    return '${hour.toString().padLeft(2, '0')}:${minute.toString().padLeft(2, '0')}';
  }

  String toDateString() {
    return '${day.toString().padLeft(2, '0')}/${month.toString().padLeft(2, '0')}/$year';
  }

  String toDateTimeString() {
    return '${toDateString()} ${toTimeString()}';
  }

  bool isSameDay(DateTime other) {
    return year == other.year && month == other.month && day == other.day;
  }

  bool isToday() {
    return isSameDay(DateTime.now());
  }

  bool isTomorrow() {
    return isSameDay(DateTime.now().add(const Duration(days: 1)));
  }

  bool isPast() {
    return isBefore(DateTime.now());
  }
}

extension BuildContextX on BuildContext {
  ThemeData get theme => Theme.of(this);
  TextTheme get textTheme => theme.textTheme;
  ColorScheme get colorScheme => theme.colorScheme;

  void showSnackBar(String message) {
    ScaffoldMessenger.of(this).showSnackBar(
      SnackBar(content: Text(message)),
    );
  }

  void showErrorSnackBar(String message) {
    ScaffoldMessenger.of(this).showSnackBar(
      SnackBar(
        content: Text(message),
        backgroundColor: Colors.red,
      ),
    );
  }
}