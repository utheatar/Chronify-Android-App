package myapp.chronify.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import myapp.chronify.data.PreferencesKey
import myapp.chronify.data.PreferencesRepository
import myapp.chronify.data.nife.NifeRepository
import java.io.File
import java.io.FileWriter

sealed class ExportState {
    object Idle : ExportState()
    object Loading : ExportState()
    object Success : ExportState()
    data class Error(val message: String) : ExportState()
}

class SettingsViewModel(
    private val repository: NifeRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    class SettingsUiState(
        val settings: Map<PreferencesKey<*>, Any> = emptyMap(),
        val isLoading: Boolean = true
    )
    /**
     * 偏好设置键列表
     */
    private val settingsKeys = listOf(
        PreferencesKey.DisplayPref.WeekStartFromSunday,
        PreferencesKey.DisplayPref.Theme,
        PreferencesKey.DisplayPref.BaseFontSize,
    )

    /**
     * 偏好设置键值对
     */
    val settingsMap = preferencesRepository.getPreferences(settingsKeys)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            // 默认值不能为 emptyMap()，否则会导致null cannot be cast to non-null type
            initialValue = buildMap {
                settingsKeys.forEach { key ->
                    put(key, key.defaultValue)
                }
            }
        )

    /**
     * 更新偏好设置
     */
    suspend fun <T> updatePreference(key: PreferencesKey<T>, value: T) {
        preferencesRepository.updatePreference(key, value)
    }

    // write csv file
    fun writeCsv(context: Context,fileName: String, data: List<List<String>>) {
        viewModelScope.launch(Dispatchers.IO) {
            writeCsvFile(context, fileName, data)
        }
    }

    fun writeCsvFile(context: Context, fileName: String, data: List<List<String>>) {
        try {
            // 获取应用专属存储目录（无需权限）
            val dir = context.getExternalFilesDir("documents") ?: return
            val file = File(dir, fileName)

            FileWriter(file).use { writer ->
                data.forEach { row ->
                    writer.append(row.joinToString(","))
                    writer.append("\n")
                }
                writer.flush()
            }
            Log.d("SettingsViewModel", "writeCsvFile: $file")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    // val settings = preferencesRepository.getPreferences(settingsKeys)
    //     .map{ prefsMap  ->
    //         SettingsUiState(
    //             weekStartFromSunday = prefsMap[PreferencesKey.DisplayPref.WeekStartFromSunday] as Boolean,
    //             theme = prefsMap[PreferencesKey.DisplayPref.Theme] as String,
    //             baseFontSize = prefsMap[PreferencesKey.DisplayPref.BaseFontSize] as Int
    //         )
    //     }
    //     .stateIn(
    //         scope = viewModelScope,
    //         started = SharingStarted.WhileSubscribed(5000),
    //         initialValue = SettingsUiState()
    //     )

    // 组合加载状态和数据流
    // val uiState: StateFlow<SettingsUiState> = preferencesRepository.getPreferences(settingsKeys)
    //     .map { prefsMap ->
    //         SettingsUiState(
    //             settings = prefsMap,
    //             isLoading = false
    //         )
    //     }
    //     .stateIn(
    //         scope = viewModelScope,
    //         started = SharingStarted.WhileSubscribed(5000),
    //         initialValue = SettingsUiState(
    //             settings = buildMap {
    //                 settingsKeys.forEach { key ->
    //                     put(key, key.defaultValue)
    //                 }
    //             },
    //             isLoading = true
    //         )
    //     )
}