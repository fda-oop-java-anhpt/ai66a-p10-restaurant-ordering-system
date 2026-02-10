package com.oop.project.model;

public class MenuCategory {
    
    private int id;
    private String name;

    public MenuCategory(int id, String name) {
         this.id = id;
         this.name = name;

    }

    // getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // setters 

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Attention
     * JList shows value from toString()
     * If forget, JList<MenuCategory> in repository will shows:
     * com.oop.project.model.MenuCategory@xxxxxxx
     * UI gets bad
     */
    
    @Override
    public String toString() {
        return name;
    }
}
