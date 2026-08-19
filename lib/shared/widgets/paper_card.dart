import 'package:flutter/material.dart';
import 'package:dotnotes/app/theme.dart';

class PaperCard extends StatelessWidget {
  final Widget child;
  final EdgeInsetsGeometry padding;
  final double radius;
  final VoidCallback? onTap;

  const PaperCard({
    super.key,
    required this.child,
    this.padding = const EdgeInsets.all(16),
    this.radius = 14,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final content = DefaultTextStyle.merge(
      style: const TextStyle(color: AppColors.ink, fontFamily: AppFonts.body),
      child: child,
    );
    final shape = RoundedRectangleBorder(
      borderRadius: BorderRadius.circular(radius),
    );

    if (onTap == null) {
      return Card(
        elevation: 0,
        color: AppColors.paper,
        shadowColor: Colors.transparent,
        shape: shape,
        child: Padding(padding: padding, child: content),
      );
    }

    return Card(
      elevation: 0,
      color: AppColors.paper,
      shadowColor: Colors.transparent,
      shape: shape,
      clipBehavior: Clip.antiAlias,
      child: InkWell(
        onTap: onTap,
        child: Padding(padding: padding, child: content),
      ),
    );
  }
}