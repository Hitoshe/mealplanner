package com.example.mealplanner.service.dto; // или ваш user.dto пакет

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// Если не используете Lombok, создайте геттеры, сеттеры, конструкторы вручную
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPhysiologyDto {
    private Double weight; // Используем Double, чтобы Thymeleaf мог оставить поле пустым
    private Double height;
    private Integer age;    // Используем Integer
    private String activityLevel; // Будет хранить имя enum (например, "LOW", "NORMAL")
}