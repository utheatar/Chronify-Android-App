package myapp.chronify.ui.element.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import myapp.chronify.R.string
import myapp.chronify.R.dimen
import myapp.chronify.data.schedule.ScheduleEntity
import myapp.chronify.ui.element.components.AppTopBar
import myapp.chronify.ui.element.components.OutLinedTextFieldWithSuggestion
import myapp.chronify.ui.navigation.NavigationRoute
import myapp.chronify.ui.viewmodel.AppViewModelProvider
import myapp.chronify.ui.viewmodel.StatisticsViewModel
import myapp.chronify.utils.toLocalDate
import java.time.LocalDate
import java.time.temporal.ChronoField
import java.util.stream.Collectors

object StatisticsScreenRoute : NavigationRoute {
    override val route = "statistics"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = viewModel(factory = AppViewModelProvider.Factory),
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val uiState by viewModel.uiState.collectAsState()
    var showSearchTip by remember { mutableStateOf(false) }
    var isCalendarView by remember { mutableStateOf(true) }
    val suggestions = uiState.suggestions

    var searchText by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }


    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AppTopBar(
                title = stringResource(string.statistics),
                scrollBehavior = scrollBehavior
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(dimensionResource(dimen.padding_small))
                .fillMaxSize()
        ) {

            // 搜索框和视图切换按钮
            Box(
                modifier = Modifier
                    .height(75.dp)
                    .zIndex(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 搜索框
                    OutLinedTextFieldWithSuggestion(
                        initialValue = uiState.searchQuery,
                        suggestions = uiState.suggestions,
                        onValueChange = {
                            viewModel.onSearchQueryChange(it)
                        },
                        label = { Text(stringResource(string.search_schedule)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    // 视图切换按钮
                    IconButton(onClick = { viewModel.toggleViewMode() }) {
                        Icon(
                            imageVector =
                            if (uiState.isCalendarView)
                                Icons.Default.ShoppingCart
                            else Icons.Default.DateRange,
                            contentDescription = stringResource(string.toggle_statistics)
                        )
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    StatisticsContent(uiState)

    // 显示选中日期的Schedules
    // selectedDate?.let { date ->
    //     val dailySchedules = schedules.filter {
    //         it.endDT?.toLocalDate() == date
    //     }
    //     if (dailySchedules.isNotEmpty()) {
    //         LazyColumn {
    //             items(dailySchedules) { schedule ->
    //                 Text(
    //                     text = "${schedule.title} - ${schedule.endDT}",
    //                     modifier = Modifier.padding(vertical = 4.dp)
    //                 )
    //             }
    //         }
    //     }
    // }
}


@Composable
fun SearchField() {
}

@Composable
fun StatisticsContent(
    uiState: StatisticsViewModel.StatisticsUiState
) {
    // if (isCalendarView) {
    //     CalendarView(
    //         schedules = schedules,
    //         searchText = searchText,
    //         onDateSelect = { selectedDate = it }
    //     )
    // } else {
    //     ChartView(
    //         schedules = schedules,
    //         searchText = searchText
    //     )
    // }
}

@Composable
fun CalendarView(
    schedules: List<ScheduleEntity>,
    searchText: String,
    onDateSelect: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    val startDate = today.minusMonths(6)
    val weeks = startDate.datesUntil(today.plusDays(1))
        .collect(Collectors.groupingBy { it.get(ChronoField.ALIGNED_WEEK_OF_YEAR) })

    LazyColumn {
        items(weeks.toList()) { (_, dates) ->
            Row {
                dates.forEach { date ->
                    val dailySchedules = schedules.filter {
                        it.endDT!!.toLocalDate() == date
                    }
                    val intensity = when {
                        dailySchedules.isEmpty() -> 0.1f
                        dailySchedules.size <= 2 -> 0.3f
                        dailySchedules.size <= 5 -> 0.6f
                        else -> 0.9f
                    }

                    val isHighlighted = searchText.isNotEmpty() &&
                            dailySchedules.any { it.title.contains(searchText, ignoreCase = true) }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .padding(2.dp)
                            .background(
                                Color.Green.copy(alpha = intensity),
                            )
                            .border(
                                width = if (isHighlighted) 2.dp else 0.dp,
                                color = if (isHighlighted) Color.Blue else Color.Transparent,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable { onDateSelect(date) }
                    )
                }
            }
        }
    }
}

// @Composable
// fun ChartView(
//     schedules: List<ScheduleEntity>,
//     searchText: String
// ) {
//     var timeRange by remember { mutableStateOf(StatisticsViewModel.TimeRange.MONTH) }
//
//     Column {
//         // 时间范围选择器
//         Row(
//             modifier = Modifier.fillMaxWidth(),
//             horizontalArrangement = Arrangement.SpaceEvenly
//         ) {
//             StatisticsViewModel.TimeRange.values().forEach { range ->
//                 Text(
//                     text = range.name,
//                     modifier = Modifier
//                         .clickable { timeRange = range }
//                         .background(
//                             if (timeRange == range) Color.LightGray else Color.Transparent
//                         )
//                         .padding(8.dp)
//                 )
//             }
//         }
//
//         // 柱状图
//         val groupedData = when (timeRange) {
//             StatisticsViewModel.TimeRange.YEAR -> schedules.groupBy { it.endDT.year }
//             StatisticsViewModel.TimeRange.MONTH -> schedules.groupBy {
//                 "${it.endDT.year}-${it.endDT.monthValue}"
//             }
//
//             StatisticsViewModel.TimeRange.WEEK -> schedules.groupBy {
//                 "${it.endDT.year}-W${it.endDT.get(ChronoField.ALIGNED_WEEK_OF_YEAR)}"
//             }
//         }
//
//         LazyColumn {
//             items(groupedData.toList()) { (period, periodSchedules) ->
//                 Row(
//                     modifier = Modifier
//                         .fillMaxWidth()
//                         .height(40.dp)
//                         .padding(vertical = 4.dp),
//                     verticalAlignment = Alignment.CenterVertically
//                 ) {
//                     Text(
//                         text = period.toString(),
//                         modifier = Modifier.width(100.dp)
//                     )
//                     Box(
//                         modifier = Modifier
//                             .height(20.dp)
//                             .width(
//                                 (periodSchedules.size * 20).coerceAtMost(200).dp
//                             )
//                             .background(Color.Green)
//                     )
//                     Text(
//                         text = periodSchedules.size.toString(),
//                         modifier = Modifier.padding(start = 8.dp)
//                     )
//                 }
//             }
//         }
//     }
// }