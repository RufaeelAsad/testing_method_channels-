import 'dart:developer';
import 'dart:io';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_overlay_window/flutter_overlay_window.dart';
import 'package:path_provider/path_provider.dart';


void main() async {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return const MaterialApp(
      home: MyHomePage(),
    );
  }
}

class MyHomePage extends StatelessWidget {
  static const platform =
      MethodChannel('com.example.hbrecorder/screenRecording');

  const MyHomePage({super.key});

  Future<void> showOverLay() async {
    try {
      final directory = await getApplicationDocumentsDirectory();
      log("path ${directory.path}");

      final Map<String, dynamic> args = <String, dynamic>{
        'outputPath': directory.path
      };
      bool isOverLayStart = await platform.invokeMethod('startOverLay', args);
      log('is overLay show $isOverLayStart');
    } on PlatformException catch (e) {
      log("Failed to stop recording: '${e.message}'.");
    }
  }

  Future<void> captureScreen() async {
    try {
      final directory = await getApplicationDocumentsDirectory();
      log("path ${directory.path}");

      final Map<String, dynamic> args = <String, dynamic>{
        'outputPath': directory.path
      };

      String path = await platform.invokeMethod('takeScreenshot', args);
      log("is capture $path");
    } on PlatformException catch (e) {
      log("Failed to stop recording: '${e.message}'.");
    }
  }

  Future<void> takeScreenShot() async {
    // String? path = await NativeScreenshot.takeScreenshot();
    //
    // log('path $path');

    await FlutterOverlayWindow.showOverlay(width: 200, height: 200);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('HBRecorder Example'),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Expanded(
                child: Image.file(File(
                    '/data/user/0/com.example.testing_chanels/app_flutter/screenshot.png'))),
            InkWell(
              onTap: () async {
                showOverLay();
                // // captureScreen();
              },
              child: Container(
                width: 200,
                height: 100,
                decoration: BoxDecoration(
                    color: Colors.orangeAccent,
                    borderRadius: BorderRadius.circular(12)),
                child: const Center(
                  child: Text(
                    "ScreenShot",
                    style: TextStyle(
                        color: Colors.white,
                        fontSize: 24,
                        fontWeight: FontWeight.bold),
                  ),
                ),
              ),
            ),
            const SizedBox(
              height: 20,
            ),
          ],
        ),
      ),
    );
  }
}

// ElevatedButton(
//   onPressed: _startRecording,
//   child: const Text('Start Recording'),
// ),
// ElevatedButton(
//   onPressed: _stopRecording,
//   child: const Text('Stop Recording'),
// ),

// Future<void> _startRecording() async {
//   try {
//     final PermissionStatus microPhone = await Permission.microphone.request();
//     log("microphone $microPhone");
//
//     final directory = await getApplicationDocumentsDirectory();
//     log("path ${directory.path}");
//
//     final Map<String, dynamic> args = <String, dynamic>{
//       'isAudioEnabled': true,
//       'outputPath': directory.path
//     };
//
//     // Start recording
//     final bool result = await platform.invokeMethod('startRecording', args);
//     log('path $result');
//   } on PlatformException catch (e) {
//     log("Failed to start recording: '${e.message}'.");
//   }
// }
//
// Future<void> _stopRecording() async {
//   try {
//     final String path = await platform.invokeMethod('stopRecording');
//     log("video path $path");
//   } on PlatformException catch (e) {
//     log("Failed to stop recording: '${e.message}'.");
//   }
// }

// Future<void> captureScreen() async {
//   try {
//     // final directory = await getApplicationDocumentsDirectory();
//     // log("path ${directory.path}");
//
//     // final Map<String, dynamic> args = <String, dynamic>{
//     //   'outputPath': directory.path
//     // };
//
//     String path = await platform.invokeMethod('takeScreenshot', args);
//     log("is capture $path");
//   } on PlatformException catch (e) {
//     log("Failed to stop recording: '${e.message}'.");
//   }
// }
