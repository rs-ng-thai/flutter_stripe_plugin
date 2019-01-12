import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:stripe_card_input/stripe_card_input.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {

  String publishKey = "pk_test_k7IuJEH2DEAXPRtitfhD05WC";
  String _token = "Input Credt Card";
  @override
  void initState() {
    super.initState();
    setPublishKey();
    StripeCardInput.setHandler(_cardInputToken);
  }

  void setPublishKey() {
    StripeCardInput.setPublishKey(publishKey);
  }

  void _showDialog() {
    StripeCardInput.showDialog();
  }

  Future<void> _cardInputToken(MethodCall call) async {
    switch(call.method) {
      case "createToken":
        debugPrint(call.arguments);
        setState(() {
          _token = call.arguments;
        });
        break;

      default:
        break;
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
              Text(
                _token != null ? _token : '',
                style: TextStyle(
                    color: Colors.red,
                    fontSize: 60.0
                ),
              ),
              FloatingActionButton(
                onPressed: _showDialog,
                tooltip: 'Show Dialog',
                child: Icon(Icons.add),
              ),
            ],
          )
        ),
      ),
    );
  }
}
