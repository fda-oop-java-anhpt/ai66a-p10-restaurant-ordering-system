package com.oop.project.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MenuItem {
    private int id;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private int categoryId;
    private LocalDateTime createdAt;

    public MenuItem(
            int id,
            String name,
            String description,
            BigDecimal basePrice,
            int categoryId,
            LocalDateTime createdAt
    ) {
        setId(id);
        setName(name);
        setDescription(description);
        setBasePrice(basePrice);
        setCategoryId(categoryId);
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("MenuItem id must be > 0");
        }
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description == null) {
            description = "";
        }
        this.description = description;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public BigDecimal getPrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price must be >= 0");
        }
        this.basePrice = basePrice;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        if (categoryId <= 0) {
            throw new IllegalArgumentException("Category ID must be > 0");
        }
        this.categoryId = categoryId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return name + " (" + basePrice + ")";
    }
}