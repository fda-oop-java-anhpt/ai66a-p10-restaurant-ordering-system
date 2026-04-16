package com.oop.project.ui.theme;

import java.awt.Insets;

public final class ThemeInsets {

    private ThemeInsets() {
    }

    public static Insets card() {
        return new Insets(AppTheme.SPACE_4, AppTheme.SPACE_4, AppTheme.SPACE_4, AppTheme.SPACE_4);
    }

    public static Insets tight() {
        return new Insets(AppTheme.SPACE_2, AppTheme.SPACE_2, AppTheme.SPACE_2, AppTheme.SPACE_2);
    }

    public static Insets section() {
        return new Insets(AppTheme.SPACE_6, AppTheme.SPACE_8, AppTheme.SPACE_6, AppTheme.SPACE_8);
    }
}