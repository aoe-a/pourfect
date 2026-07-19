package com.pourfect.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.pourfect.domain.BrewLogEntry
import com.pourfect.domain.TasteVerdict
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object BrewJournalRepository {
    private val JOURNAL = stringPreferencesKey("brew_journal")
    private const val MAX_ENTRIES = 100

    /** Newest first. */
    fun flow(context: Context): Flow<List<BrewLogEntry>> =
        context.settingsDataStore.data.map { prefs ->
            BrewLogEntry.decodeList(prefs[JOURNAL] ?: "")
        }

    suspend fun add(context: Context, entry: BrewLogEntry) {
        context.settingsDataStore.edit { prefs ->
            val existing = BrewLogEntry.decodeList(prefs[JOURNAL] ?: "")
            prefs[JOURNAL] = BrewLogEntry.encodeList(
                (listOf(entry) + existing).take(MAX_ENTRIES)
            )
        }
    }

    suspend fun setVerdict(context: Context, timestampMillis: Long, verdict: TasteVerdict) {
        context.settingsDataStore.edit { prefs ->
            val existing = BrewLogEntry.decodeList(prefs[JOURNAL] ?: "")
            prefs[JOURNAL] = BrewLogEntry.encodeList(
                existing.map {
                    if (it.timestampMillis == timestampMillis) it.copy(verdict = verdict) else it
                }
            )
        }
    }

    suspend fun setGrind(
        context: Context,
        timestampMillis: Long,
        grinderName: String?,
        grindSetting: String?
    ) {
        context.settingsDataStore.edit { prefs ->
            val existing = BrewLogEntry.decodeList(prefs[JOURNAL] ?: "")
            prefs[JOURNAL] = BrewLogEntry.encodeList(
                existing.map {
                    if (it.timestampMillis == timestampMillis) {
                        it.copy(grinderName = grinderName, grindSetting = grindSetting)
                    } else it
                }
            )
        }
    }

    suspend fun delete(context: Context, timestampMillis: Long) {
        context.settingsDataStore.edit { prefs ->
            val existing = BrewLogEntry.decodeList(prefs[JOURNAL] ?: "")
            prefs[JOURNAL] = BrewLogEntry.encodeList(
                existing.filterNot { it.timestampMillis == timestampMillis }
            )
        }
    }
}
