import 'dart:io';
import 'package:image/image.dart' as img;

void main() {
  final file = File('assets/Logo dotnotes.jpg');
  if (!file.existsSync()) {
    print('Logo file not found!');
    return;
  }

  final rawBytes = file.readAsBytesSync();
  final image = img.decodeImage(rawBytes);
  if (image == null) {
    print('Failed to decode image');
    return;
  }

  // Create 1024x1024 canvas with white background
  final canvasSize = 1024;
  final canvas = img.Image(width: canvasSize, height: canvasSize);
  img.fill(canvas, color: img.ColorRgb8(255, 255, 255));

  // Scale original logo to ~70% to fit comfortably inside the safe zone of adaptive circle mask
  final targetLogoSize = (canvasSize * 0.70).toInt();
  final resizedLogo = img.copyResize(image, width: targetLogoSize, height: targetLogoSize);

  // Center the resized logo on the white canvas
  final xOffset = (canvasSize - targetLogoSize) ~/ 2;
  final yOffset = (canvasSize - targetLogoSize) ~/ 2;

  img.compositeImage(canvas, resizedLogo, dstX: xOffset, dstY: yOffset);

  // Save as PNG
  final outputFile = File('assets/logo_foreground.png');
  outputFile.writeAsBytesSync(img.encodePng(canvas));
  print('Successfully created padded logo at: ${outputFile.path}');
}
