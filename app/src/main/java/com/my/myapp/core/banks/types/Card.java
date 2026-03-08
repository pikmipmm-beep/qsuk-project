package com.my.myapp.core.banks.types;

public class Card {
    private final String requisites;
    private final int amount;

    // Конструктор с двумя параметрами
    public Card(String requisites, int amount) {
        this.requisites = requisites;
        this.amount = amount;
    }

    // Геттер для requisites
    public String getRequisites() {
        return requisites;
    }

    // Геттер для amount
    public int getAmount() {
        return amount;
    }
}