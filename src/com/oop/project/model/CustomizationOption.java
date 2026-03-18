package com.oop.project.model;

import java.math.BigDecimal;

public class CustomizationOption {
    private final int id;
    private final String name;
    private final BigDecimal priceDelta;
    private final int menuItemId;

    public CustomizationOption(int id, String name, BigDecimal priceDelta, int menuItemId) {
        this.id = id;
        this.name = name;
        this.priceDelta = priceDelta;
        this.menuItemId = menuItemId;
    }

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

    @Override
    public String toString() {
        if (priceDelta.compareTo(BigDecimal.ZERO) == 0) {
            return name;
        }
        String sign = priceDelta.compareTo(BigDecimal.ZERO) > 0 ? "+" : "";
        return name + " (" + sign + priceDelta.stripTrailingZeros().toPlainString() + ")";
    }
}
