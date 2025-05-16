package com.example.mealplanner.service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private String name;
    private String proteinStr;
    private String fatsStr;
    private String carbsStr;
    private String caloriesStr;
    private int gamma = 100; // Значение по умолчанию
}