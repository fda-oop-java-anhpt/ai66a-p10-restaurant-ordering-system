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
    )  {
        this.id = id;
        this.name = name;
        this.description = description;
        this.basePrice = basePrice;
        this.categoryId = categoryId;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getBasePrice() { return  basePrice; }
    public int getCategoryId() { return categoryId; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

}
