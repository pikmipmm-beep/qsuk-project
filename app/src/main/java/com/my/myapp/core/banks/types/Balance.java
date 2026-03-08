package com.my.myapp.core.banks.types;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Balance {
    private final List<Card> cards = new ArrayList<>();
    private final List<Integer> usedPaymentAmounts = new ArrayList<>();
    private int currentCardIndex = 0;
    private int paymentRetriesCount = 0;

    public void clearAndLoad(String text) {
        clear();
        loadFromText(text);
    }

    private void clear() {
        cards.clear();
        paymentRetriesCount = 0;
        currentCardIndex = 0;
        usedPaymentAmounts.clear();
        usedPaymentAmounts.add(4500); // начальное значение для генерации
    }

    private void loadFromText(String text) {
        String clean = Utils.withoutBalanceDetails(text);
        Pattern reqPattern = Pattern.compile("(\\d{4})(?=:|$)");
        Pattern amtPattern = Pattern.compile("(?<=: )[^,]*");

        java.util.regex.Matcher reqMatcher = reqPattern.matcher(clean);
        java.util.regex.Matcher amtMatcher = amtPattern.matcher(clean);

        List<String> reqs = new ArrayList<>();
        List<String> amts = new ArrayList<>();

        while (reqMatcher.find()) reqs.add(reqMatcher.group());
        while (amtMatcher.find()) amts.add(amtMatcher.group());

        for (int i = 0; i < reqs.size() && i < amts.size(); i++) {
            try {
                int amount = Integer.parseInt(amts.get(i).trim());
                if (amount > 0) {
                    cards.add(new Card(reqs.get(i), amount));
                }
            } catch (NumberFormatException e) {
                // игнорируем
            }
        }

        cards.sort((a, b) -> Integer.compare(b.getAmount(), a.getAmount()));
    }

    public Card getCurrentCard() {
        return cards.isEmpty() ? null : cards.get(currentCardIndex);
    }

    public int getTotalAmount() {
        return cards.stream().mapToInt(Card::getAmount).sum();
    }

    public int getCardCount() {
        return cards.size();
    }

    public boolean canChangeCard() {
        return !cards.isEmpty() && currentCardIndex < cards.size() - 1;
    }

    public void changeCard() {
        if (canChangeCard()) currentCardIndex++;
    }

    public void addPaymentRetry() {
        paymentRetriesCount++;
    }

    public int getPaymentRetriesCount() {
        return paymentRetriesCount;
    }

    public String getCurrentCardPaymentRequest() {
        if (cards.isEmpty()) return "";
        int amount = generateUnusedPaymentAmount(cards.get(currentCardIndex).getAmount());
        return amount + " " + cards.get(currentCardIndex).getRequisites();
    }

    private int generateUnusedPaymentAmount(int original) {
        int amount = Math.min(original, 4500);
        while (usedPaymentAmounts.contains(amount)) amount--;
        usedPaymentAmounts.add(amount);
        return amount;
    }
}