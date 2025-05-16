package com.example.mealplanner.service;

import com.example.mealplanner.business.model.Category;
import com.example.mealplanner.business.model.Product;
import com.example.mealplanner.dataaccess.ProductRepository;
import com.example.mealplanner.service.dto.ProductDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct; // Для Java EE / Jakarta EE
// import javax.annotation.PostConstruct; // Для Java SE (до Java 11), если jakarta не работает, попробуй этот

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements com.example.mealplanner.service.ProductService {

    private final ProductRepository productRepository;
    private List<Category> categoriesCache;

    // Путь к оригинальному файлу в ресурсах
    @Value("${mealplanner.data.initial-products-file:data/products.xml}")
    private String initialXmlFilePath;

    // Путь к файлу, который будет редактироваться. Может быть тем же, что и initial,
    // или другим, например, в домашней директории пользователя или рядом с JAR.
    @Value("${mealplanner.data.editable-products-file:products_catalog_data.xml}")
    private String editableXmlFilePath;


    @Autowired
    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @PostConstruct // Этот метод будет вызван после создания бина и внедрения зависимостей
    public void initializeCache() {
        loadCategories();
    }

    private synchronized void loadCategories() { // Синхронизировано на случай параллельного доступа
        try {
            File editableFile = new File(editableXmlFilePath);
            if (editableFile.exists() && editableFile.length() > 0) { // Проверяем, что файл не пустой
                this.categoriesCache = productRepository.loadCategoriesFromXml(editableXmlFilePath);
                System.out.println("Загружены категории из редактируемого файла: " + editableFile.getAbsolutePath());
            } else {
                this.categoriesCache = productRepository.loadCategoriesFromXml(initialXmlFilePath);
                System.out.println("Загружены категории из начального файла: " + initialXmlFilePath);
                // Если initial файл тоже пуст или не найден, categoriesCache будет пустым
                // Можно скопировать initial в editable, если editable не существует
                if (this.categoriesCache != null && !this.categoriesCache.isEmpty() && !editableFile.exists()) {
                    // saveCategories(); // Раскомментируй, если хочешь, чтобы начальные данные копировались в editable файл при первом запуске, если editable пуст
                }
            }
        } catch (IOException e) {
            System.err.println("Критическая ошибка загрузки каталога продуктов: " + e.getMessage());
            e.printStackTrace();
            this.categoriesCache = new ArrayList<>(); // Инициализируем пустым списком в случае серьезной ошибки
        }
        if (this.categoriesCache == null) { // Дополнительная проверка на случай, если productRepository вернул null
            this.categoriesCache = new ArrayList<>();
        }
    }

    private synchronized void saveCategories() { // Синхронизировано
        try {
            productRepository.saveCategoriesToXml(editableXmlFilePath, this.categoriesCache);
            System.out.println("Каталог продуктов сохранен в: " + new File(editableXmlFilePath).getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Ошибка сохранения каталога продуктов: " + e.getMessage());
            e.printStackTrace();
            // Здесь можно выбросить RuntimeException или специфическое сервисное исключение, чтобы оповестить вызывающий код
        }
    }

    @Override
    public List<Category> getAllCategories() {
        if (categoriesCache == null) { // Дополнительная проверка, хотя initializeCache должен был это сделать
            loadCategories();
        }
        // Возвращаем глубокую копию, чтобы избежать внешних модификаций кэша
        // Это важно, если другие части приложения могут изменять полученный список
        return categoriesCache.stream()
                .map(c -> {
                    Category copyCat = new Category();
                    copyCat.setName(c.getName());
                    copyCat.setDescription(c.getDescription());
                    // Продукты тоже копируем, чтобы избежать модификации объектов в кэше
                    List<Product> copiedProducts = c.getProducts().stream()
                            .map(p -> new Product(p.getName(), p.getGramms(), p.getProteinStr(), p.getFatsStr(), p.getCarbsStr(), p.getCaloriesStr()))
                            .collect(Collectors.toList());
                    copyCat.setProducts(copiedProducts);
                    return copyCat;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Category> searchProductsByName(String searchTerm) {
        if (categoriesCache == null) loadCategories();
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllCategories(); // Если поиск пустой, возвращаем все
        }
        String lowerCaseSearchTerm = searchTerm.toLowerCase().trim();
        return categoriesCache.stream()
                .map(category -> {
                    List<Product> filteredProducts = category.getProducts().stream()
                            .filter(product -> product.getName().toLowerCase().contains(lowerCaseSearchTerm))
                            .collect(Collectors.toList());
                    if (!filteredProducts.isEmpty()) {
                        Category newCategory = new Category();
                        newCategory.setName(category.getName());
                        newCategory.setDescription(category.getDescription());
                        newCategory.setProducts(filteredProducts); // Здесь продукты не копируются глубоко, т.к. это результат поиска
                        return newCategory;
                    }
                    return null;
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public void addCategory(String categoryName, String description) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            System.err.println("Имя категории не может быть пустым");
            // Можно бросить: throw new IllegalArgumentException("Имя категории не может быть пустым");
            return;
        }
        if (categoriesCache == null) loadCategories();

        String trimmedCategoryName = categoryName.trim();
        boolean exists = categoriesCache.stream().anyMatch(cat -> cat.getName().equalsIgnoreCase(trimmedCategoryName));
        if (exists) {
            System.err.println("Категория с именем '" + trimmedCategoryName + "' уже существует.");
            // Можно бросить: throw new IllegalArgumentException("Категория с именем '" + trimmedCategoryName + "' уже существует.");
            return;
        }
        Category newCategory = new Category(trimmedCategoryName, description != null ? description.trim() : "", new ArrayList<>());
        categoriesCache.add(newCategory);
        saveCategories();
    }

    @Override
    public void addProductToCategory(String categoryName, ProductDto productDto) {
        if (categoriesCache == null) loadCategories();
        Optional<Category> categoryOpt = categoriesCache.stream()
                .filter(cat -> cat.getName().equalsIgnoreCase(categoryName))
                .findFirst();

        if (categoryOpt.isPresent()) {
            Category category = categoryOpt.get();
            String trimmedProductName = productDto.getName().trim();
            boolean productExists = category.getProducts().stream()
                    .anyMatch(p -> p.getName().equalsIgnoreCase(trimmedProductName));
            if(productExists){
                System.err.println("Продукт '" + trimmedProductName + "' уже существует в категории '" + categoryName + "'.");
                // Можно бросить: throw new IllegalArgumentException("Продукт '" + trimmedProductName + "' уже существует в категории '" + categoryName + "'.");
                return;
            }

            Product newProduct = new Product();
            newProduct.setName(trimmedProductName);
            // ИСПРАВЛЕНО: Используем setGramms и productDto.getGamma()
            newProduct.setGramms(productDto.getGamma());
            newProduct.setProteinStr(productDto.getProteinStr());
            newProduct.setFatsStr(productDto.getFatsStr());
            newProduct.setCarbsStr(productDto.getCarbsStr());
            newProduct.setCaloriesStr(productDto.getCaloriesStr());

            if (newProduct.isValid()) {
                category.getProducts().add(newProduct);
                saveCategories();
            } else {
                System.err.println("Некорректные данные для продукта: " + trimmedProductName);
                // Можно бросить: throw new IllegalArgumentException("Некорректные данные для продукта: " + trimmedProductName);
            }
        } else {
            System.err.println("Категория '" + categoryName + "' не найдена.");
            // Можно бросить: throw new IllegalArgumentException("Категория '" + categoryName + "' не найдена.");
        }
    }

    @Override
    public void removeCategory(String categoryName) {
        if (categoriesCache == null) loadCategories();
        boolean removed = categoriesCache.removeIf(cat -> cat.getName().equalsIgnoreCase(categoryName));
        if (removed) {
            saveCategories();
        } else {
            System.err.println("Категория '" + categoryName + "' для удаления не найдена.");
        }
    }

    @Override
    public void removeProductFromCategory(String categoryName, String productName) {
        if (categoriesCache == null) loadCategories();
        Optional<Category> categoryOpt = categoriesCache.stream()
                .filter(cat -> cat.getName().equalsIgnoreCase(categoryName))
                .findFirst();

        if (categoryOpt.isPresent()) {
            Category category = categoryOpt.get();
            boolean removed = category.getProducts().removeIf(prod -> prod.getName().equalsIgnoreCase(productName));
            if (removed) {
                saveCategories();
            } else {
                System.err.println("Продукт '" + productName + "' в категории '" + categoryName + "' для удаления не найден.");
            }
        } else {
            System.err.println("Категория '" + categoryName + "' для удаления продукта не найдена.");
        }
    }
}