package com.oop.project.model;

public class CustomizationOption {

    private int id;
    private String name;
    private double priceDelta;
    private int menuItemId;

    // Constructor
    public CustomizationOption(int id, String name, double priceDelta, int menuItemId) {
        this.id = id;
        this.name = name;
        this.priceDelta = priceDelta;
        this.menuItemId = menuItemId;
    }

    // Getter
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPriceDelta() {
        return priceDelta;
    }

    public int getMenuItemId() {
        return menuItemId;
    }

    // Setter (optional nếu cần sửa)
    public void setName(String name) {
        this.name = name;
    }

    public void setPriceDelta(double priceDelta) {
        this.priceDelta = priceDelta;
    }

    public void setMenuItemId(int menuItemId) {
        this.menuItemId = menuItemId;
    }

    // toString (debug rất tiện)
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