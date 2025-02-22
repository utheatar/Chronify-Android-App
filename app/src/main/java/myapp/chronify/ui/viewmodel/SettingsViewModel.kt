package myapp.chronify.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.material3.SnackbarDuration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import myapp.chronify.data.PreferencesKey
import myapp.chronify.data.PreferencesRepository
import myapp.chronify.data.nife.Nife
import myapp.chronify.data.nife.NifeRepository
import myapp.chronify.data.nife.NifeType
import myapp.chronify.data.nife.PeriodType
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
    fun writeCsv(
        context: Context,
        fileName: String = "Nifes.csv",
        exportDir: String = "exports"
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            writeToCsvFile(
                context,
                fileName,
                repository.getAllNifes().first(),
                exportDir = exportDir
            )
        }
    }

    private fun writeToCsvFile(
        context: Context,
        fileName: String,
        data: List<Nife>,
        exportDir: String
    ) {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        try {
            // 获取应用专属存储目录（无需权限）
            val dir = context.getExternalFilesDir(exportDir) ?: return
            val file = File(dir, fileName)

            file.bufferedWriter(Charsets.UTF_8, bufferSize = 1024).use { writer ->
                data.forEach { nife ->
                    writer.append(
                        buildString {
                            append("\"${nife.title.replace("\"", "\"\"")}\"").append(",")
                            append(nife.type).append(",")
                            append(nife.isFinished).append(",")
                            append(nife.createdDT.format(dateFormatter)).append(",")
                            append(nife.beginDT?.format(dateFormatter) ?: "").append(",")
                            append(nife.endDT?.format(dateFormatter) ?: "").append(",")
                            append(nife.period?.name ?: "").append(",")
                            append(nife.periodMultiple).append(",")
                            append("\"${nife.triggerTimes.joinToString(";")}\"").append(",")
                            append("\"${nife.description.replace("\"", "\"\"")}\"").append(",")
                            append("\"${nife.location.replace("\"", "\"\"")}\"")
                            append("\n")
                        }
                    )
                }
                writer.flush()
            }
            Log.d("SettingsViewModel", "writeToCsvFile: $file")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // read csv file
    fun importIntoDB(
        context: Context,
        fileName: String = "Nifes.csv",
        importDir: String = "import",
    ) {
        val nifes = readFromCsvFile(context, fileName, importDir)
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertAll(nifes)
        }
    }

    private fun readFromCsvFile(
        context: Context,
        fileName: String = "Nifes.csv",
        importDir: String = "import"
    ): List<Nife> {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val nifes = mutableListOf<Nife>()
        try {
            // 获取应用专属存储目录（无需权限）
            val dir = context.getExternalFilesDir(importDir) ?: return nifes
            val file = File(dir, fileName)
            // 判断文件是否存在
            if (!file.exists()) {
                Log.e("CSV", "File not found: ${file.absolutePath}")
                return nifes
            }

            file.bufferedReader(Charsets.UTF_8, bufferSize = 1024).use { reader ->
                reader.forEachLine { line ->
                    // 自定义 CSV 解析逻辑（处理带引号的字段）
                    val fields = parseCsvLine(line).toMutableList()
                    if (fields.size < 11) { // 根据你的字段数量调整
                        Log.w("CSV", "Invalid line: $line")
                        return@forEachLine
                    }
                    // 构建 Nife 对象
                    val nife = try {
                        Nife(
                            title = fields[0].unescapeCsvField(),
                            type = NifeType.valueOf(fields[1]),
                            isFinished = fields[2].toBoolean(),
                            createdDT = LocalDateTime.parse(fields[3], dateFormatter),
                            beginDT = fields[4].takeIf { s: String -> s.isNotBlank() }
                                ?.let { s: String -> LocalDateTime.parse(s, dateFormatter) },
                            endDT = fields[5].takeIf { s: String -> s.isNotBlank() }
                                ?.let { s: String -> LocalDateTime.parse(s, dateFormatter) },
                            period = if (fields[6].isNotBlank())
                                PeriodType.valueOf(fields[6]) else null,
                            periodMultiple = fields[7].toIntOrNull() ?: 1,
                            triggerTimes = fields[8].unescapeCsvField()
                                .split(";")
                                .asSequence()
                                .map { it.trim() }
                                .filter { it.isNotBlank() }
                                .mapNotNull { it.toIntOrNull() }
                                .toSet(),
                            description = fields[9].unescapeCsvField(),
                            location = fields[10].unescapeCsvField()
                        )
                    } catch (e: Exception) {
                        Log.e("CSV", "Parse error: ${e.message}\nLine: $line  \n" +
                                " Fields: $fields")
                        null
                    }
                    if (nife != null)
                        nifes.add(nife)
                    // Log.d("SettingsViewModel", "readFromCsvFile: $line")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return nifes
    }

    private fun parseCsvLine(line: String): List<String> {
        val fields = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        for (c in line) {
            when {
                c == ',' && !inQuotes -> {
                    fields.add(sb.toString())
                    sb.clear()
                }

                c == '"' -> inQuotes = !inQuotes
                else -> sb.append(c)
            }
        }
        fields.add(sb.toString())
        return fields
    }

    // 处理 CSV 字段转义（Kotlin 扩展函数）
    private fun String.unescapeCsvField(): String {
        return this.trim()
            .removeSurrounding("\"")
            .replace("\"\"", "\"")
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