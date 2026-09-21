package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "bubble_shooter_prefs")

class GamePreferencesRepository(private val context: Context) {

    private val KEY_UNLOCKED_LEVEL = intPreferencesKey("highest_unlocked_level")
    private val KEY_SFX_ENABLED = booleanPreferencesKey("sound_fx_enabled")
    private val KEY_MUSIC_ENABLED = booleanPreferencesKey("music_enabled")

    private fun highScoreKey(levelId: Int) = intPreferencesKey("level_${levelId}_high_score")
    private fun starsKey(levelId: Int) = intPreferencesKey("level_${levelId}_stars")

    val highestUnlockedLevel: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_UNLOCKED_LEVEL] ?: 1
    }

    val soundFxEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_SFX_ENABLED] ?: true
    }

    val musicEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_MUSIC_ENABLED] ?: true
    }

    fun getLevelHighScore(levelId: Int): Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[highScoreKey(levelId)] ?: 0
    }

    fun getLevelStars(levelId: Int): Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[starsKey(levelId)] ?: 0
    }

    suspend fun saveLevelResult(levelId: Int, score: Int, stars: Int, totalLevelsCount: Int) {
        context.dataStore.edit { preferences ->
            val curHigh = preferences[highScoreKey(levelId)] ?: 0
            if (score > curHigh) {
                preferences[highScoreKey(levelId)] = score
            }

            val curStars = preferences[starsKey(levelId)] ?: 0
            if (stars > curStars) {
                preferences[starsKey(levelId)] = stars
            }

            // Unlock next level if stars >= 1
            if (stars >= 1) {
                val currentUnlocked = preferences[KEY_UNLOCKED_LEVEL] ?: 1
                val nextLevel = (levelId + 1).coerceAtMost(totalLevelsCount)
                if (nextLevel > currentUnlocked) {
                    preferences[KEY_UNLOCKED_LEVEL] = nextLevel
                }
            }
        }
    }

    suspend fun setSoundFxEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SFX_ENABLED] = enabled
        }
    }

    suspend fun setMusicEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_MUSIC_ENABLED] = enabled
        }
    }

    suspend fun clearAllData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
