package myapp.chronify.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import myapp.chronify.data.PreferencesRepository
import myapp.chronify.data.nife.NifeRepository
import myapp.chronify.utils.daysUntil
import java.time.LocalDate

class StatisticsViewModel(
    private val repository: NifeRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {
    enum class TimeRange {
        WEEK, MONTH, YEAR
    }

    // UI States
    data class StatisticsUiState(
        val searchQuery: String = "",
        val suggestions: List<String> = emptyList(),
        val isCalendarView: Boolean = true,
        val selectedDate: LocalDate? = null,
        // val selectedDateSchedules: List<Schedule> = emptyList(),
        val timeRange: TimeRange = TimeRange.MONTH,
        val isLoading: Boolean = false,
        val error: String? = null
    )

    data class DayStatistics(
        val date: LocalDate,
        val count: Int,
        val isHighlighted: Boolean = false
    )

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    // 处理搜索框输入
    fun onSearchQueryChange(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(searchQuery = query) }
            // 获取标题建议
            if (query.isNotBlank()) {
                val suggestions = repository.getSimilarTitles(query).first()
                _uiState.update { it.copy(suggestions = suggestions) }
            } else {
                _uiState.update { it.copy(suggestions = emptyList()) }
            }
            // 更新统计数据
            updateStatistics()
        }
    }

    // 切换视图模式
    fun toggleViewMode() {
        _uiState.update { it.copy(isCalendarView = !it.isCalendarView) }
        updateStatistics()
    }

    // // 选择日期
    // fun selectDate(date: LocalDate) {
    //     viewModelScope.launch {
    //         val schedules = repository.getSchedulesByDate(date).first()
    //             .map {
    //                 schedule -> schedule.toSchedule()
    //             }
    //         _uiState.update {
    //             it.copy(
    //                 selectedDate = date,
    //                 selectedDateSchedules = schedules
    //             )
    //         }
    //     }
    // }
    //
    // // 更改时间范围（周/月/年）
    // fun setTimeRange(timeRange: TimeRange) {
    //     _uiState.update { it.copy(timeRange = timeRange) }
    //     updateStatistics()
    // }

    // 更新统计数据
    private fun updateStatistics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                if (_uiState.value.isCalendarView) {
                    // updateCalendarData()
                } else {
                    // updateChartData()
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message ?: "An error occurred",
                        isLoading = false
                    )
                }
            }
        }
    }


}