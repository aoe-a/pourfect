package com.pourfect.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.settingsDataStore by preferencesDataStore(name = "settings")

data class UserSettings(
    val vibrationEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val showOunces: Boolean = false,
    val useFahrenheit: Boolean = false,
    val voiceEnabled: Boolean = false,
    val myGrinderId: String? = null
)

object SettingsRepository {
    private val VIBRATION = booleanPreferencesKey("vibration_enabled")
    private val SOUND = booleanPreferencesKey("sound_enabled")
    private val OUNCES = booleanPreferencesKey("show_ounces")
    private val FAHRENHEIT = booleanPreferencesKey("use_fahrenheit")
    private val VOICE = booleanPreferencesKey("voice_enabled")
    private val MY_GRINDER = stringPreferencesKey("my_grinder_id")

    fun flow(context: Context): Flow<UserSettings> =
        context.settingsDataStore.data.map { prefs ->
            UserSettings(
                vibrationEnabled = prefs[VIBRATION] ?: true,
                soundEnabled = prefs[SOUND] ?: true,
                showOunces = prefs[OUNCES] ?: false,
                useFahrenheit = prefs[FAHRENHEIT] ?: false,
                voiceEnabled = prefs[VOICE] ?: false,
                myGrinderId = prefs[MY_GRINDER]?.takeIf { it.isNotBlank() }
            )
        }

    suspend fun setVibration(context: Context, enabled: Boolean) {
        context.settingsDataStore.edit { it[VIBRATION] = enabled }
    }

    suspend fun setSound(context: Context, enabled: Boolean) {
        context.settingsDataStore.edit { it[SOUND] = enabled }
    }

    suspend fun setShowOunces(context: Context, enabled: Boolean) {
        context.settingsDataStore.edit { it[OUNCES] = enabled }
    }

    suspend fun setUseFahrenheit(context: Context, enabled: Boolean) {
        context.settingsDataStore.edit { it[FAHRENHEIT] = enabled }
    }

    suspend fun setVoiceEnabled(context: Context, enabled: Boolean) {
        context.settingsDataStore.edit { it[VOICE] = enabled }
    }

    suspend fun setMyGrinder(context: Context, grinderId: String?) {
        context.settingsDataStore.edit { it[MY_GRINDER] = grinderId ?: "" }
    }
}
