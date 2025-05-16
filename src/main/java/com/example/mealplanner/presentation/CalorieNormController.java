package com.example.mealplanner.presentation; // или ваш user.presentation пакет

import com.example.mealplanner.business.model.ActivityLevel;
import com.example.mealplanner.service.CalorieNormService;
import com.example.mealplanner.service.dto.UserPhysiologyDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user") // Опционально: сгруппировать все URL, связанные с пользователем, под /user
public class CalorieNormController {

    private final CalorieNormService calorieNormService;

    @Autowired
    public CalorieNormController(CalorieNormService calorieNormService) {
        this.calorieNormService = calorieNormService;
    }

    // Метод для отображения формы и результатов
    @GetMapping("/calculate-norm")
    public String showCalculationForm(Model model) {
        // Добавляем пустой DTO для связывания с формой, если он не пришел через RedirectAttributes
        if (!model.containsAttribute("userPhysiologyDto")) {
            model.addAttribute("userPhysiologyDto", new UserPhysiologyDto());
        }
        // Передаем все возможные уровни активности для выпадающего списка
        model.addAttribute("activityLevels", ActivityLevel.getAllValues());
        return "user"; // Путь к нашему новому HTML файлу
    }

    // Метод для обработки данных формы
    @PostMapping("/calculate-norm")
    public String calculateNorm(@ModelAttribute("userPhysiologyDto") UserPhysiologyDto userDto,
                                RedirectAttributes redirectAttributes) {
        try {
            double dailyNorm = calorieNormService.calculateDailyNorm(userDto);
            redirectAttributes.addFlashAttribute("successMessage", "Ваша дневная норма калорий рассчитана!");
            redirectAttributes.addFlashAttribute("calculatedNorm", dailyNorm);
            // Если нужно показать BMR, сервис должен его возвращать, и мы его тоже передадим
            // redirectAttributes.addFlashAttribute("calculatedBmr", bmrValue);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            // Возвращаем введенные пользователем данные обратно в форму
            redirectAttributes.addFlashAttribute("userPhysiologyDto", userDto);
        }
        return "redirect:/user/calculate-norm"; // Редирект обратно на GET для отображения формы/результата
    }
}