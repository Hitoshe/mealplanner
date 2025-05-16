package com.example.mealplanner.service;

import com.example.mealplanner.business.model.Category;
import com.example.mealplanner.service.dto.ProductDto;

import java.util.List;

public interface ProductService {
    List<Category> getAllCategories();
    List<Category> searchProductsByName(String searchTerm);

    void addCategory(String categoryName, String description);
    void addProductToCategory(String categoryName, ProductDto productDto);

    void removeCategory(String categoryName);
    void removeProductFromCategory(String categoryName, String productName);

    // TODO добавить методы для обновления
    // void updateCategory(String oldName, CategoryDto categoryDto);
    // void updateProductInCategory(String categoryName, String oldProductName, ProductDto productDto);
}