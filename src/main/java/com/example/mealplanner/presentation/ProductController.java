package com.example.mealplanner.presentation;

import com.example.mealplanner.business.model.Category;
import com.example.mealplanner.service.ProductService;
import com.example.mealplanner.service.dto.ProductDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping({"/", "/catalog"})
    public String showCatalog(Model model, @RequestParam(required = false) String searchTerm) {
        List<Category> categoriesToDisplay; // Категории для основного отображения (могут быть отфильтрованы)
        if (searchTerm != null && !searchTerm.isEmpty()) {
            categoriesToDisplay = productService.searchProductsByName(searchTerm);
            model.addAttribute("searchTerm", searchTerm);
        } else {
            categoriesToDisplay = productService.getAllCategories();
        }
        model.addAttribute("categories", categoriesToDisplay); // Для отображения списка

        // Всегда передаем ПОЛНЫЙ список категорий для выпадающих списков в формах
        List<Category> allCategoriesForForms = productService.getAllCategories();
        model.addAttribute("allAvailableCategories", allCategoriesForForms);

        if (!model.containsAttribute("productDto")) {
            model.addAttribute("productDto", new ProductDto());
        }
        if (!model.containsAttribute("newCategoryName")) model.addAttribute("newCategoryName", "");
        if (!model.containsAttribute("newCategoryDescription")) model.addAttribute("newCategoryDescription", "");

        return "catalog";
    }

    @PostMapping("/catalog/add-category")
    public String addCategory(@RequestParam String categoryName,
                              @RequestParam(required = false) String categoryDescription, // Описание опционально
                              RedirectAttributes redirectAttributes) {
        try {
            productService.addCategory(categoryName, categoryDescription);
            redirectAttributes.addFlashAttribute("successMessage", "Категория '" + categoryName + "' успешно добавлена.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            // Сохраняем введенные данные для отображения в форме
            redirectAttributes.addFlashAttribute("newCategoryName", categoryName);
            redirectAttributes.addFlashAttribute("newCategoryDescription", categoryDescription);
        }
        return "redirect:/catalog";
    }

    @PostMapping("/catalog/add-product")
    public String addProduct(@RequestParam String categoryNameForProduct,
                             @ModelAttribute ProductDto productDto,
                             RedirectAttributes redirectAttributes) {
        try {
            productService.addProductToCategory(categoryNameForProduct, productDto);
            redirectAttributes.addFlashAttribute("successMessage", "Продукт '" + productDto.getName() + "' успешно добавлен в категорию '" + categoryNameForProduct + "'.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            // Сохраняем введенные данные DTO для отображения в форме
            redirectAttributes.addFlashAttribute("productDto", productDto);
            // Также нужно передать обратно имя выбранной категории, если это возможно и нужно
            // redirectAttributes.addFlashAttribute("selectedCategoryForProductAdd", categoryNameForProduct);
        }
        return "redirect:/catalog";
    }

    @PostMapping("/catalog/remove-category")
    public String removeCategory(@RequestParam String categoryNameToRemove, RedirectAttributes redirectAttributes) {
        productService.removeCategory(categoryNameToRemove);
        redirectAttributes.addFlashAttribute("successMessage", "Категория '" + categoryNameToRemove + "' удалена.");
        return "redirect:/catalog";
    }

    @PostMapping("/catalog/remove-product")
    public String removeProduct(@RequestParam String categoryNameForProductRemove,
                                @RequestParam String productNameToRemove,
                                RedirectAttributes redirectAttributes) {
        productService.removeProductFromCategory(categoryNameForProductRemove, productNameToRemove);
        redirectAttributes.addFlashAttribute("successMessage", "Продукт '" + productNameToRemove + "' удален из категории '" + categoryNameForProductRemove + "'.");
        return "redirect:/catalog";
    }
}