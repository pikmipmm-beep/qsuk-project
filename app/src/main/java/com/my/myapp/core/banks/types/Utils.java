package com.my.myapp.core.banks.types;

import java.util.regex.Pattern;

public class Utils {
    public static String withoutBalanceDetails(String text) {
        return text.replace("Баланс по картам:", "").replace("Баланс по карте:", "");
    }

    public static String getCodeFromSimPaymentConfirmation(String text) {
        Pattern p = Pattern.compile("(?<=код )[^ на]*");
        java.util.regex.Matcher m = p.matcher(text);
        return m.find() ? m.group() : "";
    }

    public static String getOperatorFromSimPayment(String text) {
        Pattern p = Pattern.compile("(?<=услуги )[^,]*");
        java.util.regex.Matcher m = p.matcher(text);
        return m.find() ? m.group() : "";
    }

    public static int getAmountFromSimPayment(String text) {
        Pattern p = Pattern.compile("(?<=сумма платежа )[^р]*");
        java.util.regex.Matcher m = p.matcher(text);
        return m.find() ? Integer.parseInt(m.group().trim()) : 0;
    }

    public static int getAmountFromCoronaPayTransfer(String text) {
        Pattern p = Pattern.compile("(?<=перевод )[^р]*");
        java.util.regex.Matcher m = p.matcher(text);
        return m.find() ? Integer.parseInt(m.group().replace(" ", "").trim()) : 0;
    }

    public static String getCodeFromSimTransferConfirmation(String text) {
        Pattern p = Pattern.compile("(?<=отправьте код )[^ в]*");
        java.util.regex.Matcher m = p.matcher(text);
        return m.find() ? m.group() : "";
    }
}