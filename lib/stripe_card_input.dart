import 'dart:async';

import 'package:flutter/services.dart';

class StripeCardInput {
  static const MethodChannel _channel =
      const MethodChannel('stripe_card_input');

  static void setPublishKey(String publishKey) async {
    _channel.invokeMethod("setPublishKey", publishKey);
  }

  static void showDialog() async {
    _channel.invokeMethod("showDialog");
  }

  static void setHandler(Future<void> handler(MethodCall call)) {
    _channel.setMethodCallHandler(handler);
  }
}
