package myapp.chronify.ui.viewmodel

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.map
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import myapp.chronify.data.PreferencesRepository
import myapp.chronify.data.nife.Nife
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
        val dateEventMap: Map<LocalDate, List<Nife>> = emptyMap(),
        val timeRange: TimeRange = TimeRange.MONTH,
        val isLoading: Boolean = true,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // 动态响应的 PagingData
    @OptIn(ExperimentalCoroutinesApi::class)
    val nifesPagingData: Flow<PagingData<Nife>> = searchQuery
        .flatMapLatest { query ->
            repository.getFinishedNifesByTitleAsPgFlow(query)
        }
        .cachedIn(viewModelScope)

    // 添加一个状态来存储转换后的Map
    private val _dateEventMap = MutableStateFlow<Map<LocalDate, List<Nife>>>(emptyMap())
    val dateEventMap: StateFlow<Map<LocalDate, List<Nife>>> = _dateEventMap.asStateFlow()


    // init {
    //     viewModelScope.launch {
    //         // 在ViewModel中监听PagingData的变化并更新 dateEventMap
    //         nifesPagingData.collectLatest { pagingData ->
    //             val dateMap = mutableMapOf<LocalDate, MutableList<Nife>>()
    //             pagingData.map { nife ->
    //                 nife.endDT?.toLocalDate()?.let { date ->
    //                     dateMap.getOrPut(date) { mutableListOf() }.add(nife)
    //                 }!!
    //             }
    //
    //             _uiState.update { currentState ->
    //                 currentState.copy(dateEventMap = dateMap)
    //             }
    //             _dateEventMap.value = dateMap
    //         }
    //
    //     }
    // }

    // 处理搜索框输入
    fun onSearchQueryChange(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(searchQuery = query) }
            _searchQuery.value = query // 更新 StateFlow
            // 获取标题建议
            if (query.isNotBlank()) {
                val suggestions = repository.getSimilarTitles(query).first()
                _uiState.update { it.copy(suggestions = suggestions) }
            } else {
                _uiState.update { it.copy(suggestions = emptyList()) }
            }
            // update dateMap
            // _uiState.update { it.copy(
            //     dateEventMap = convertToDateEventMap(nifesPagingData)
            // ) }
        }
    }

    // 切换视图模式
    fun toggleViewMode() {
        _uiState.update { it.copy(isCalendarView = !it.isCalendarView) }
    }

    fun convertToDateEventMap(lazyItems: LazyPagingItems<Nife>): Map<LocalDate, List<Nife>> {
        val dateEventMap = mutableMapOf<LocalDate, MutableList<Nife>>()

        // 遍历所有已加载的项目
        for (index in 0 until lazyItems.itemCount) {
            val nife = lazyItems[index] ?: continue

            // 获取完成日期作为key
            val date = nife.endDT?.toLocalDate() ?: continue

            // 将事件添加到对应日期的列表中
            dateEventMap.getOrPut(date) { mutableListOf() }.add(nife)
        }

        return dateEventMap
    }
}