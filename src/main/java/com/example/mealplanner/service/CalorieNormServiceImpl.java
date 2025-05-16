package com.example.mealplanner.service;

import com.example.mealplanner.business.model.ActivityLevel;
import com.example.mealplanner.service.dto.UserPhysiologyDto;
import org.springframework.stereotype.Service;

@Service // Важно для Spring, чтобы он создал бин этого класса
public class CalorieNormServiceImpl implements CalorieNormService {

    @Override
    public double calculateDailyNorm(UserPhysiologyDto userDto) {
        // Простая валидация (можно расширить или использовать аннотации @Valid в контроллере)
        if (userDto.getWeight() == null || userDto.getWeight() <= 0 ||
                userDto.getHeight() == null || userDto.getHeight() <= 0 ||
                userDto.getAge() == null || userDto.getAge() <= 0 ||
                userDto.getActivityLevel() == null || userDto.getActivityLevel().trim().isEmpty()) {
            throw new IllegalArgumentException("Все поля (вес, рост, возраст, уровень активности) должны быть заполнены корректно.");
        }

        double weight = userDto.getWeight();
        double height = userDto.getHeight();
        int age = userDto.getAge();

        // Рассчитываем BMR (формула для женщин, как в задании)
        // BMR = 447.593 + 9.247 * Вес + 3.098 * Рост - 4.330 * Возраст
        double bmr = 447.593 + (9.247 * weight) + (3.098 * height) - (4.330 * age);

        if (bmr <= 0) { // Дополнительная проверка на адекватность BMR
            throw new IllegalArgumentException("Введенные данные привели к некорректному BMR. Проверьте вес, рост и возраст.");
        }

        ActivityLevel activityLevel;
        try {
            // Преобразуем строку из DTO в наш enum
            activityLevel = ActivityLevel.valueOf(userDto.getActivityLevel().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Некорректный уровень активности: " + userDto.getActivityLevel());
        }

        double armCoefficient = activityLevel.getCoefficient();

        // Дневная норма калорий = BMR * ARM
        double dailyCalorieNorm = bmr * armCoefficient;

        // Округляем до двух знаков после запятой для красивого вывода (опционально)
        return Math.round(dailyCalorieNorm * 100.0) / 100.0;
    }
}