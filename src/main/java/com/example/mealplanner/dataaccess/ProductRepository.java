package com.example.mealplanner.dataaccess;

import com.example.mealplanner.business.model.Category;
import java.io.IOException;
import java.util.List;

public interface ProductRepository {
    List<Category> loadCategoriesFromXml(String filePath) throws IOException;
    void saveCategoriesToXml(String filePath, List<Category> categories) throws IOException;
}