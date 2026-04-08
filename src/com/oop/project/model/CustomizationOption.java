package com.oop.project.model;
import java.math.BigDecimal;
public class CustomizationOption {

    private int id;
    private String name;
    private BigDecimal priceDelta;
    private int menuItemId;

    // Constructor rỗng
    public CustomizationOption() {
    }

    // Constructor chính
    public CustomizationOption(int id, String name, BigDecimal priceDelta, int menuItemId) {
        setId(id);
        setName(name);
        setPriceDelta(priceDelta);
        setMenuItemId(menuItemId);
    }

    // Copy constructor
    public CustomizationOption(CustomizationOption other) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot copy null object");
        }
        this.id = other.id;
        this.name = other.name;
        this.priceDelta = other.priceDelta;
        this.menuItemId = other.menuItemId;
    }

    // Getter
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPriceDelta() {
        return priceDelta;
    }

    public int getMenuItemId() {
        return menuItemId;
    }

    // Setter
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPriceDelta(BigDecimal priceDelta) {
        this.priceDelta = priceDelta;
    }

    public void setMenuItemId(int menuItemId) {
        if (menuItemId <= 0) {
            throw new IllegalArgumentException("menuItemId must be > 0");
        }
        this.menuItemId = menuItemId;
    }

    @Override
    public String toString() {
        return "CustomizationOption{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", priceDelta=" + priceDelta +
                ", menuItemId=" + menuItemId +
                '}';
    }
}