package com.example.mealplanner.business.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Category")
public class Category {

    @XmlAttribute(name = "name")
    private String name;

    @XmlAttribute(name = "description")
    private String description;

    @XmlElement(name = "Product")
    private List<Product> products = new ArrayList<>();

    public boolean isValid() {
        return name != null && !name.trim().isEmpty();
    }
}