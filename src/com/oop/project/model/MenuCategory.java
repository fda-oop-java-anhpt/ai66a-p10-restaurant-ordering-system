package com.oop.project.model;

public class MenuCategory {
    
    private int id;
    private String name;

    public MenuCategory(int id, String name) {
         this.id = id;
         this.name = name;

    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
