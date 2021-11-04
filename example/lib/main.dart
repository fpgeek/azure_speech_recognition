import 'dart:async';

import 'package:audioplayers/audioplayers.dart';
import 'package:azure_speech_recognition/azure_speech_recognition.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _centerText = 'Unknown';
  AzureSpeechRecognition? _speechAzure;
  String subKey = "24c42b66e7c642d98ded85270b6863eb";
  String region = "koreacentral";
  String lang = "en-US";
  bool isRecording = false;

  void activateSpeechRecognizer() {
    // MANDATORY INITIALIZATION
    AzureSpeechRecognition.initialize(subKey, region,
        lang: lang, recording: true);

    _speechAzure?.setFinalTranscription((text) async {
      // do what you want with your final transcription
      setState(() {
        _centerText = text;
        isRecording = false;
      });
    });

    _speechAzure?.setRecognitionStartedHandler(() {
      // called at the start of recognition (it could also not be used)
      isRecording = true;
    });
    _speechAzure?.setRecognitionStoppedHandler((recordFilePath) async {
      print('recordFilePath: $recordFilePath');
      // final file = File(recordFilePath);
      // final bytes = file.readAsBytesSync();
      // print('bytes: ${bytes.length}');
      final audioPlayer = AudioPlayer();
      await audioPlayer.play(
        recordFilePath,
        isLocal: true,
      );
      setState(() {
        isRecording = false;
      });
    });
  }

  @override
  void initState() {
    _speechAzure = AzureSpeechRecognition();
    activateSpeechRecognizer();

    super.initState();
  }

  Future _recognizeVoice() async {
    try {
      await AzureSpeechRecognition
          .continuousRecording(); //await platform.invokeMethod('azureVoice');

    } on PlatformException catch (e) {
      print("Failed to get text '${e.message}'.");
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            children: <Widget>[
              Text('TEXT RECOGNIZED : $_centerText\n'),
              FloatingActionButton(
                onPressed: () {
                  if (!isRecording) _recognizeVoice();
                },
                child: Icon(Icons.mic),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
