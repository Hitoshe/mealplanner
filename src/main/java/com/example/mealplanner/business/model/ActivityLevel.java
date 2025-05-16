package com.example.mealplanner.business.model;

public enum ActivityLevel {
    LOW(1.2, "Низкий"),
    NORMAL(1.375, "Нормальный"),
    MEDIUM(1.55, "Средний"),
    HIGH(1.725, "Высокий");

    private final double coefficient;
    private final String displayName;

    ActivityLevel(double coefficient, String displayName) {
        this.coefficient = coefficient;
        this.displayName = displayName;
    }

    public double getCoefficient() {
        return coefficient;
    }

    public String getDisplayName() {
        return displayName;
    }

    // Это нужно, чтобы Thymeleaf мог получить все значения enum для выпадающего списка
    public static ActivityLevel[] getAllValues() {
        return values();
    }
}