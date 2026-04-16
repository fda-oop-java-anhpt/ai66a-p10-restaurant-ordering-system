package com.oop.project.ui.theme;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;

public final class ThemeFonts {

    private static final String MANROPE_REGULAR = "/fonts/Manrope-Regular.ttf";
    private static final String INTER_REGULAR = "/fonts/Inter-Regular.ttf";

    private static final Font DISPLAY_FALLBACK = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font BODY_FALLBACK = new Font("Segoe UI", Font.PLAIN, 14);

    private static Font displayFont = DISPLAY_FALLBACK;
    private static Font bodyFont = BODY_FALLBACK;
    private static boolean initialized;

    private ThemeFonts() {
    }

    public static synchronized void initialize() {
        if (initialized) {
            return;
        }

        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

            Font manrope = loadFont(MANROPE_REGULAR);
            Font inter = loadFont(INTER_REGULAR);

            ge.registerFont(manrope);
            ge.registerFont(inter);

            displayFont = manrope;
            bodyFont = inter;
        } catch (Exception ex) {
            displayFont = DISPLAY_FALLBACK;
            bodyFont = BODY_FALLBACK;
        }

        initialized = true;
    }

    public static Font displayLg() {
        return displayFont.deriveFont(Font.BOLD, 44f);
    }

    public static Font displayMd() {
        return displayFont.deriveFont(Font.BOLD, 28f);
    }

    public static Font headlineSm() {
        return displayFont.deriveFont(Font.PLAIN, 24f);
    }

    public static Font titleLg() {
        return bodyFont.deriveFont(Font.BOLD, 20f);
    }

    public static Font bodyLg() {
        return bodyFont.deriveFont(Font.PLAIN, 16f);
    }

    public static Font bodyMd() {
        return bodyFont.deriveFont(Font.PLAIN, 14f);
    }

    public static Font labelMd() {
        return bodyFont.deriveFont(Font.PLAIN, 12f);
    }

    public static Font labelSm() {
        return bodyFont.deriveFont(Font.PLAIN, 11f);
    }

    private static Font loadFont(String resourcePath) throws Exception {
        try (InputStream stream = ThemeFonts.class.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new IOException("Missing font resource: " + resourcePath);
            }
            return Font.createFont(Font.TRUETYPE_FONT, stream);
        }
    }
}