package com.pourfect.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.pourfect.domain.CustomRecipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object CustomRecipeRepository {
    private val RECIPES = stringPreferencesKey("custom_recipes")

    fun flow(context: Context): Flow<List<CustomRecipe>> =
        context.settingsDataStore.data.map { prefs ->
            CustomRecipe.decodeList(prefs[RECIPES] ?: "")
        }

    /** Saves the recipe, replacing any existing recipe with the same name. */
    suspend fun save(context: Context, recipe: CustomRecipe) {
        context.settingsDataStore.edit { prefs ->
            val existing = CustomRecipe.decodeList(prefs[RECIPES] ?: "")
            val updated = existing.filterNot { it.name == recipe.name } + recipe
            prefs[RECIPES] = CustomRecipe.encodeList(updated)
        }
    }

    suspend fun delete(context: Context, name: String) {
        context.settingsDataStore.edit { prefs ->
            val existing = CustomRecipe.decodeList(prefs[RECIPES] ?: "")
            prefs[RECIPES] = CustomRecipe.encodeList(existing.filterNot { it.name == name })
        }
    }
}
