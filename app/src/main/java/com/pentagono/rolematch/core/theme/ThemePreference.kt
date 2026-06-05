package com.pentagono.rolematch.core.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// ─── Enum ─────────────────────────────────────────────────────────────────────

enum class ThemeMode(val label: String) {
    SYSTEM("Seguir sistema"),
    LIGHT("Modo claro"),
    DARK("Modo escuro")
}

// ─── DataStore extension ──────────────────────────────────────────────────────

private val Context.themeDataStore: DataStore<Preferences>
    by preferencesDataStore(name = "theme_prefs")

// ─── Manager ──────────────────────────────────────────────────────────────────

class ThemePreferenceManager(private val context: Context) {

    companion object {
        private val THEME_KEY = stringPreferencesKey("theme_mode")
    }

    /** Emits the current ThemeMode whenever it changes. */
    val themeMode: Flow<ThemeMode> = context.themeDataStore.data.map { prefs ->
        val raw = prefs[THEME_KEY] ?: ThemeMode.SYSTEM.name
        ThemeMode.valueOf(raw)
    }

    /** Persist the chosen mode. */
    suspend fun setThemeMode(mode: ThemeMode) {
        context.themeDataStore.edit { prefs ->
            prefs[THEME_KEY] = mode.name
        }
    }
}
