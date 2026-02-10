package com.oop.project.service;

import java.util.List;

import com.oop.project.model.MenuCategory;
import com.oop.project.model.MenuItem;
import com.oop.project.repository.MenuCategoryRepository;
import com.oop.project.repository.MenuItemRepository;

public class MenuService {
    
    protected final MenuCategoryRepository categoryRepo = new MenuCategoryRepository();
    protected final MenuItemRepository itemRepo = new MenuItemRepository();

    public List<MenuCategory> getAllCategories() {
        return categoryRepo.findAll();
    }

    public List<MenuItem> getMenuItemsByCategory(int categoryId) {
        return itemRepo.findbyCategory(categoryId);
    }
}
