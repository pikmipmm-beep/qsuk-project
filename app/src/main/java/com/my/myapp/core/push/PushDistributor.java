package com.my.myapp.core.push;

public class PushDistributor {
    private final Push push;

    public PushDistributor(Push push) {
        this.push = push;
    }

    public boolean isPushEmpty() {
        return push.title == null || push.title.isEmpty() || push.text == null || push.text.isEmpty();
    }

    public boolean isPushProhibited() {
        String[] blacklist = {
            "com.android.vending", "com.android.systemui", "com.android.chrome",
            "com.google.android.apps.maps", "com.google.android.youtube",
            "com.whatsapp", "com.instagram.android", "com.facebook.katana"
        };
        for (String pkg : blacklist) {
            if (push.packageName.equals(pkg)) return true;
        }
        return false;
    }

    public boolean containsKoronaPayTransferCompletion() {
        return push.packageName.contains("sber") && push.title.contains("Корона");
    }
}