package com.example.mealplanner.business.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Product")
public class Product {

    @XmlElement(name = "Name")
    private String name;

    @XmlElement(name = "Gramms")
    private int gramms; // Вес в граммах, для которого указаны БЖУК (обычно 100)

    // Храним как строки из XML из-за возможного использования запятой как десятичного разделителя
    @XmlElement(name = "Protein")
    private String proteinStr;

    @XmlElement(name = "Fats")
    private String fatsStr;

    @XmlElement(name = "Carbs")
    private String carbsStr;

    @XmlElement(name = "Calories")
    private String caloriesStr;

    // Вспомогательный метод для парсинга строки в double, заменяя запятую на точку
    private double parseDoubleSafely(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(value.replace(",", "."));
        } catch (NumberFormatException e) {
            System.err.println("Ошибка парсинга значения '" + value + "' в Product: " + e.getMessage());
            return 0.0; // Возвращаем 0 в случае ошибки
        }
    }

    // Геттеры для получения числовых значений
    public double getProtein() {
        return parseDoubleSafely(proteinStr);
    }

    public double getFats() {
        return parseDoubleSafely(fatsStr);
    }

    public double getCarbs() {
        return parseDoubleSafely(carbsStr);
    }

    public double getCalories() {
        return parseDoubleSafely(caloriesStr);
    }

    // Сеттеры для строковых полей (используются JAXB и могут быть использованы DTO)
    public void setProteinStr(String proteinStr) { this.proteinStr = proteinStr; }
    public void setFatsStr(String fatsStr) { this.fatsStr = fatsStr; }
    public void setCarbsStr(String carbsStr) { this.carbsStr = carbsStr; }
    public void setCaloriesStr(String caloriesStr) { this.caloriesStr = caloriesStr; }


    // Валидация (пример)
    public boolean isValid() {
        return name != null && !name.trim().isEmpty() &&
                proteinStr != null && !proteinStr.trim().isEmpty() && // Проверяем строки
                fatsStr != null && !fatsStr.trim().isEmpty() &&
                carbsStr != null && !carbsStr.trim().isEmpty() &&
                caloriesStr != null && !caloriesStr.trim().isEmpty() &&
                getCalories() >= 0; // Числовое значение калорий должно быть не отрицательным
    }
}