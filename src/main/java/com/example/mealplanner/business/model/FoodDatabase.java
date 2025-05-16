package com.example.mealplanner.business.model; // Убедись, что пакет правильный!

import lombok.Data;
import lombok.NoArgsConstructor;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@XmlRootElement(name = "Db") // Соответствует корневому тегу <Db>
@XmlAccessorType(XmlAccessType.FIELD)
public class FoodDatabase {

    @XmlElement(name = "Category")
    private List<Category> categories = new ArrayList<>();
}