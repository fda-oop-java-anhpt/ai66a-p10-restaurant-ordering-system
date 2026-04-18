package com.oop.project.ui.theme;

import java.awt.Color;
import java.awt.Font;
public final class AppTheme {
    // Color tokens
    public static final Color SURFACE = new Color(0xF8F9FA);
    public static final Color SURFACE_CONTAINER = new Color(0xEDEEEF);
    public static final Color SURFACE_CONTAINER_LOW = new Color(0xF3F4F5);
    public static final Color SURFACE_CONTAINER_LOWEST = Color.WHITE;
    public static final Color SURFACE_CONTAINER_HIGH = new Color(0xE2E3E4);
    public static final Color OUTLINE = new Color(200, 200, 200);
    public static final Color PRIMARY = new Color(0x162839);
    public static final Color PRIMARY_CONTAINER = new Color(0x2C3E50);
    public static final Color ON_PRIMARY = Color.WHITE;

    public static final Color SECONDARY = new Color(0x006D37);
    public static final Color ON_SECONDARY = Color.WHITE;

    public static final Color ERROR = new Color(0xBA1A1A);
    public static final Color ON_BACKGROUND = new Color(0x191C1D);
    public static final Color ON_SURFACE_VARIANT = new Color(0x43474E);
    public static final Color OUTLINE_VARIANT = new Color(0xC4C6CD);

    // Spacing tokens
    public static final int SPACE_1 = 4;
    public static final int SPACE_2 = 8;
    public static final int SPACE_3 = 12;
    public static final int SPACE_4 = 16;
    public static final int SPACE_6 = 24;
    public static final int SPACE_8 = 32;

    // Radius tokens
    public static final int RADIUS_SM = 6;
    public static final int RADIUS_MD = 8;
    public static final int RADIUS_LG = 12;
    public static final int RADIUS_XL = 16;

    // background root
    public static final Color BACKGROUND = new Color(0xF5F6F7);

    // text semantic
    public static final Color TEXT_PRIMARY = ON_BACKGROUND;
    public static final Color TEXT_SECONDARY = ON_SURFACE_VARIANT;

    // error states
    public static final Color ERROR_SOFT = new Color(255, 235, 238);     
    public static final Color ERROR_CONTAINER = new Color(255, 205, 210); 

    // focus ring 
    public static final Color FOCUS_RING = new Color(0x4C9AFF);

    // font 
    public static final Font FONT_DISPLAY_LARGE = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font FONT_DISPLAY_MEDIUM = new Font("Segoe UI", Font.BOLD, 22);

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);

    public static final Font FONT_BODY_LARGE = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_SECONDARY = new Font("Segoe UI", Font.PLAIN, 12);
    
    public static final Font FONT_LABEL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 13);
    private AppTheme() {
    }
}