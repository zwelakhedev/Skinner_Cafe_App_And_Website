package za.co.skinnercafe.app.ui.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(
    name = "app_preferences"
)

class AppPreferences(context: Context) {

    private val dataStore: DataStore<Preferences> =
        context.applicationContext.dataStore

    private companion object {
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val NOTIFICATIONS_ENABLED =
            booleanPreferencesKey("notifications_enabled")
    }

    val isDarkTheme: Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[DARK_THEME] ?: false
        }

    val notificationsEnabled: Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[NOTIFICATIONS_ENABLED] ?: true
        }

    suspend fun setDarkTheme(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DARK_THEME] = enabled
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }
}