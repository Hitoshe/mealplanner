package com.example.mealplanner.dataaccess;

import com.example.mealplanner.business.model.Category;
import com.example.mealplanner.business.model.Product;
import com.example.mealplanner.business.model.FoodDatabase; // Тут мы импортируем новый класс
import org.springframework.stereotype.Repository;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Repository
public class XmlProductRepository implements ProductRepository {


    @Override
    public List<Category> loadCategoriesFromXml(String filePath) throws IOException {
        File xmlFile = getFileFromResourcesOrExternal(filePath);
        if (!xmlFile.exists() || xmlFile.length() == 0) {
            // Если файл не существует или пустой (особенно актуально для editable файла),
            // возвращаем пустой список.
            System.out.println("Файл " + filePath + " не найден или пуст. Будет возвращен пустой список категорий.");
            return new ArrayList<>();
        }

        try {
            // JAXBContext теперь должен знать о FoodDatabase, Category и Product
            JAXBContext jaxbContext = JAXBContext.newInstance(FoodDatabase.class, Category.class, Product.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            // Десериализуем весь XML в объект FoodDatabase
            // Используем InputStreamReader для указания кодировки UTF-8
            FoodDatabase foodDb;
            try (InputStream inputStream = new FileInputStream(xmlFile)) {
                foodDb = (FoodDatabase) jaxbUnmarshaller.unmarshal(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            }

            if (foodDb != null && foodDb.getCategories() != null) {
                return foodDb.getCategories();
            } else {
                // Если foodDb null или у него нет категорий, это может быть проблемой с XML или маппингом
                System.err.println("Не удалось получить категории из XML файла: " + filePath + ". Возможно, файл пуст или имеет неверную структуру корневого элемента.");
                return new ArrayList<>();
            }

        } catch (JAXBException e) {
            // Добавим больше деталей в сообщение об ошибке
            String errorMessage = "Ошибка парсинга XML (JAXB) для файла " + xmlFile.getAbsolutePath() + ": " + e.getMessage();
            Throwable cause = e.getCause();
            if (cause != null) {
                errorMessage += ". Причина: " + cause.getMessage();
            }
            System.err.println(errorMessage);
            e.printStackTrace(); // Выводим полный стектрейс для диагностики
            throw new IOException(errorMessage, e);
        } catch (FileNotFoundException e) {
            // Это исключение может быть выброшено FileInputStream, если файл исчез между проверкой и открытием
            System.err.println("Файл не найден при попытке чтения: " + xmlFile.getAbsolutePath());
            throw new IOException("Файл не найден при попытке чтения: " + xmlFile.getAbsolutePath(), e);
        }
    }

    @Override
    public void saveCategoriesToXml(String filePath, List<Category> categories) throws IOException {
        File xmlFile = new File(filePath);

        File parentDir = xmlFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new IOException("Не удалось создать директорию: " + parentDir.getAbsolutePath());
            }
        }

        try {
            // JAXBContext теперь должен знать о FoodDatabase, Category и Product
            JAXBContext jaxbContext = JAXBContext.newInstance(FoodDatabase.class, Category.class, Product.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true); // Для красивого форматирования XML
            jaxbMarshaller.setProperty(Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8.name()); // Устанавливаем кодировку UTF-8

            FoodDatabase foodDbToSave = new FoodDatabase();
            foodDbToSave.setCategories(categories); // Устанавливаем список категорий в наш корневой объект

            try (OutputStream os = new FileOutputStream(xmlFile)) {
                jaxbMarshaller.marshal(foodDbToSave, os);
            }
        } catch (JAXBException e) {
            System.err.println("Ошибка сохранения XML: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Ошибка сохранения XML: " + e.getMessage(), e);
        }
    }

    private File getFileFromResourcesOrExternal(String fileName) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        java.net.URL resourceUrl = classLoader.getResource(fileName);

        if (resourceUrl != null) {
            // Файл найден в resources (classpath)
            try {
                return new File(resourceUrl.toURI());
            } catch (java.net.URISyntaxException e) {
                // Эта ошибка маловероятна для корректных путей в classpath, но лучше обработать
                System.err.println("Ошибка получения URI для ресурса: " + fileName);
                throw new IOException("Ошибка получения URI для ресурса: " + fileName, e);
            }
        } else {
            // Если не найден в resources, пробуем как обычный файл (относительно корня проекта или абсолютный путь)
            File externalFile = new File(fileName);
            // Для метода loadCategoriesFromXml, если файл не существует, он вернет пустой список категорий.
            // Для saveCategoriesToXml файл будет создан, если его нет.
            return externalFile;
        }
    }
}