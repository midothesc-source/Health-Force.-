package com.example.ui.theme

import android.content.Context

object ThemePreferences {
    private const val PREFS_NAME = "healthforce_theme_prefs"
    private const val KEY_THEME_ID = "selected_theme_id"

    fun loadTheme(context: Context): AppThemeOption {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedId = prefs.getString(KEY_THEME_ID, null)
        return AppThemeOption.fromId(savedId)
    }

    fun saveTheme(context: Context, theme: AppThemeOption) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME_ID, theme.id)
            .apply()
    }
}
