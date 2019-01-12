package com.shareclarity.stripecardinput;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Token;
import com.stripe.android.view.CardInputWidget;
import com.stripe.android.view.CardMultilineWidget;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/** StripeCardInputPlugin */
public class StripeCardInputPlugin implements MethodCallHandler {
  String publishKey;
  Stripe stripe;
  static Activity mActivity;
  static Dialog dialog;
  static CardInputWidget cardInputWidget;
  static MethodChannel channel;
  public static final int SCAN_CARD_REQUEST_CODE = 1;
  /** Plugin registration. */
  public static void registerWith(Registrar registrar) {
    channel = new MethodChannel(registrar.messenger(), "stripe_card_input");
    channel.setMethodCallHandler(new StripeCardInputPlugin());
    mActivity = registrar.activity();
    registrar.addActivityResultListener(new PluginRegistry.ActivityResultListener() {
      @Override
      public boolean onActivityResult(int i, int i1, Intent intent) {
        if (i == SCAN_CARD_REQUEST_CODE) {
          String resultDisplayStr;
          if (intent != null && intent.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
            CreditCard scanResult = intent.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);

            // Never log a raw card number. Avoid displaying it, but if necessary use getFormattedCardNumber()
            cardInputWidget.setCardNumber(scanResult.cardNumber);

            // Do something with the raw number, e.g.:
            // myService.setCardNumber( scanResult.cardNumber );

            if (scanResult.isExpiryValid()) {
              cardInputWidget.setExpiryDate(scanResult.expiryMonth, scanResult.expiryYear);
            }

            if (scanResult.cvv != null) {
              // Never log or display a CVV
              cardInputWidget.setCvcCode(scanResult.cvv);
            }
          }
          else {

          }
          // do something with resultDisplayStr, maybe display it in a textView
          // resultTextView.setText(resultDisplayStr);
        }
        return false;
      }
    });

  }

  @Override
  public void onMethodCall(MethodCall call, final Result result) {
    if (call.method.equals("showDialog")) {
      mActivity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          showDialog(result);
        }
      });

    }
    else if (call.method.equals("setPublishKey")) {
      publishKey = (String)call.arguments;
    }
    else {
      result.notImplemented();
    }
  }

  private void showDialog(final Result result) {
    if (stripe == null) {
      if (publishKey == null) {
        result.success(false);
        return;
      }
      stripe = new Stripe(mActivity,publishKey);
    }
    if (dialog == null) {
      dialog = new Dialog(mActivity);
      dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
      dialog.setContentView(R.layout.stripe_dialog);
      dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
      dialog.setCancelable(true);

      cardInputWidget = dialog.findViewById(R.id.card_input_widget);

      dialog.findViewById(R.id.imv_scan).setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          Intent scanIntent = new Intent(mActivity, CardIOActivity.class);

          scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true);
          scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, true);
          mActivity.startActivityForResult(scanIntent, SCAN_CARD_REQUEST_CODE);
        }
      });
      dialog.findViewById(R.id.buttonCancel).setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          result.success(false);
          dialog.dismiss();
        }
      });
      dialog.findViewById(R.id.buttonSave).setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          if (cardInputWidget.getCard() == null) {
            result.success(false);
            return;
          }
          stripe.createToken(cardInputWidget.getCard(), new TokenCallback() {
            @Override
            public void onError(Exception error) {
              return;
            }

            @Override
            public void onSuccess(Token token) {
              channel.invokeMethod("createToken", token.getId());
              return;
            }
          });
        }
      });
    }
    dialog.show();
    result.success(true);
  }


}
