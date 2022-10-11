package com.todolist.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
//import com.todolist.ui.tasks.SortOrder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "PrefrencesManager"

enum class SortOrder { BY_NAME, BY_DATE }

data class FilterPreferences(val sortOrder: SortOrder, val hideCompleted: Boolean)

@Singleton
class PrefrencesManager @Inject constructor(@ApplicationContext context: Context) {
    private val Context._dataStore: DataStore<Preferences> by preferencesDataStore("user_preferences")
    private val dataStore = context._dataStore

    val prefrencesFlow = dataStore.data
        .catch { exception ->


            if (exception is IOException) {
                Log.e(TAG, "Error reading Preferences", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            val sortOrder = SortOrder.valueOf(
                preferences[PrefrencesKeys.SORT_ORDERS] ?: SortOrder.BY_DATE.name
            )
            val hideCompleted = preferences[PrefrencesKeys.HIDE_COMPLETED] ?: false
            FilterPreferences(sortOrder, hideCompleted)
        }


    suspend fun updateSortOrder(sortOrder: SortOrder) {
        dataStore.edit { preferences ->
            preferences[PrefrencesKeys.SORT_ORDERS] = sortOrder.name
        }
    }


    suspend fun updateHideCompleted(hideCompleted: Boolean) {
        dataStore.edit { preferences ->
            preferences[PrefrencesKeys.HIDE_COMPLETED] = hideCompleted
        }
    }


    private object PrefrencesKeys {
        val SORT_ORDERS = stringPreferencesKey("sort_order")
        val HIDE_COMPLETED = booleanPreferencesKey("hide_completed")


    }
}