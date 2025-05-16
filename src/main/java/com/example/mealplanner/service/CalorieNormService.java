package com.example.mealplanner.service; // или ваш user.service пакет

import com.example.mealplanner.service.dto.UserPhysiologyDto;

public interface CalorieNormService {
    // Для простоты, результат будем хранить в простом Double
    // Если захотите возвращать и BMR, и норму, можно сделать CalorieNormResultDto
    double calculateDailyNorm(UserPhysiologyDto userDto);
}