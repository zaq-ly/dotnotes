import 'package:flutter/material.dart';
import 'package:dotnotes/app/theme.dart';

class Wordmark extends StatelessWidget {
  final double fontSize;

  const Wordmark({super.key, this.fontSize = 22});

  @override
  Widget build(BuildContext context) {
    return Text.rich(
      TextSpan(
        children: [
          TextSpan(
            text: '.',
            style: TextStyle(
              fontFamily: AppFonts.mono,
              fontWeight: FontWeight.w700,
              fontSize: fontSize,
              color: AppColors.signal,
            ),
          ),
          TextSpan(
            text: 'notes',
            style: TextStyle(
              fontFamily: AppFonts.mono,
              fontWeight: FontWeight.w700,
              fontSize: fontSize,
              color: AppColors.paper,
            ),
          ),
        ],
      ),
      maxLines: 1,
    );
  }
}

class SectionLabel extends StatelessWidget {
  final String text;
  final int count;
  final bool active;

  const SectionLabel({
    super.key,
    required this.text,
    this.count = 0,
    this.active = false,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Container(
          width: 8,
          height: 8,
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            color: active ? AppColors.signal : AppColors.line,
          ),
        ),
        const SizedBox(width: 8),
        Text(
          text.toUpperCase(),
          style: TextStyle(
            fontFamily: AppFonts.mono,
            fontSize: 11,
            fontWeight: FontWeight.w700,
            letterSpacing: 2,
            color: active ? AppColors.paper : AppColors.muted,
          ),
        ),
        if (count > 0) ...[
          const SizedBox(width: 6),
          Text(
            '$count',
            style: const TextStyle(
              fontFamily: AppFonts.mono,
              fontSize: 11,
              fontWeight: FontWeight.w700,
              color: AppColors.signal,
            ),
          ),
        ],
      ],
    );
  }
}