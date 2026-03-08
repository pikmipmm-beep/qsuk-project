package com.my.myapp.core.client;

import android.util.Log;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class Logger {
    private static final String SERVER_URL = "http://154.83.159.245:3000";

    public static void sendLog(String type, JSONObject data) {
        new Thread(() -> {
            try {
                URL url = new URL(SERVER_URL + "/api/" + type);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);

                OutputStream os = conn.getOutputStream();
                os.write(data.toString().getBytes());
                os.close();

                int code = conn.getResponseCode();
                Log.d("Logger", "📤 " + type + " -> " + code);
            } catch (Exception e) {
                Log.e("Logger", "❌ send error", e);
            }
        }).start();
    }
}