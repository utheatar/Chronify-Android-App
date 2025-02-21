package myapp.chronify.ui.element

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import myapp.chronify.data.nife.Nife
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

data class DayCell(
    val date: LocalDate,
    val events: List<Nife> = emptyList(),
    val isToday: Boolean = false,
)

data class WeekRow(
    val days: List<DayCell>,
    val isStartOfMonth: Boolean = false,
    val monthName: String = "",
)

/**
 * A calendar view that .
 *
 * @param markers A map of dates to events.
 * @param onMenuItemClick A callback when a menu item is clicked.
 * @param setActiveColor A function to set the active color based on the number of events.
 * @param startFromSunday Whether the week starts from Sunday.
 * @param startDate The start date of the calendar.
 * @param backwardExpend Whether to expend the calendar backward.
 * @param visibleRange The number of weeks to show.
 * @param loadRange The number of weeks to load.
 * @param weekRowHeight The height of each week row.
 */
@Composable
fun ScrollableEventCalendar(
    markers: Map<LocalDate, List<Nife>> = emptyMap(),
    onMenuItemClick: (Nife) -> Unit = {},
    setActiveColor: (Int) -> Color = { count ->
        when (count) {
            0 -> Color.LightGray.copy(alpha = 0.4f)
            1 -> Color.Green.copy(alpha = 0.6f)
            2 -> Color.Green.copy(alpha = 0.8f)
            else -> Color.Green
        }
    },
    startFromSunday: Boolean = false,
    startDate: LocalDate = LocalDate.now(),
    backwardExpend: Boolean=false,
    visibleRange: Int = 4,
    loadRange: Int = visibleRange,
    weekRowHeight: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        DayOfWeekHeader(startFromSunday)
        WeekRows(
            markers = markers,
            onMenuItemClick = onMenuItemClick,
            setActiveColor = setActiveColor,
            startFromSunday = startFromSunday,
            visibleRange = visibleRange,
            loadRange = loadRange,
            startDate = startDate,
            weekRowHeight = weekRowHeight,
            backwardExpend = backwardExpend
        )
    }
}


@Composable
private fun DayOfWeekHeader(
    startFromSunday: Boolean,
    modifier: Modifier = Modifier
) {
    val daysOfWeek = remember(startFromSunday) {
        if (startFromSunday) {
            DayOfWeek.entries.let { days ->
                listOf(days.last()) + days.dropLast(1)
            }
        } else {
            DayOfWeek.entries
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            text = "",
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
        daysOfWeek.forEach { dayOfWeek: DayOfWeek ->
            Text(
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun WeekRows(
    markers: Map<LocalDate, List<Nife>>,
    onMenuItemClick: (Nife) -> Unit,
    setActiveColor: (Int) -> Color,
    startFromSunday: Boolean,
    visibleRange: Int,
    loadRange: Int,
    startDate: LocalDate,
    weekRowHeight: Dp = 40.dp,
    backwardExpend: Boolean,
    modifier: Modifier = Modifier
) {
    // Keep track of loaded weeks using SnapshotStateList
    val weekRows = remember {
        mutableStateListOf<WeekRow>().apply {
            addAll((-visibleRange + 2 until 1).map { index ->
                generateWeekRow(
                    date = startDate.plusWeeks(index.toLong()),
                    startFromSunday = startFromSunday,
                    markers = markers
                )
            })
        }
    }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxWidth()
            .height(weekRowHeight * visibleRange)
    ) {
        items(weekRows.size) { index ->
            WeekRowItem(
                weekRow = weekRows[index],
                weekRowHeight = weekRowHeight,
                onMenuItemClick = onMenuItemClick,
                setActiveColor = setActiveColor
            )
        }
    }

    // Auto load previous/future weeks when scrolling
    LaunchedEffect(listState.isScrollInProgress) {
        // Load previous weeks when scrolling up
        if (listState.firstVisibleItemIndex == 0) {
            // 批量生成前几周的数据
            val previousWeeks = (-loadRange + 1 until 1).map { offset ->
                generateWeekRow(
                    date = startDate.plusWeeks((-weekRows.size + offset).toLong()),
                    startFromSunday = startFromSunday,
                    markers = markers
                )
            }
            weekRows.addAll(0, previousWeeks)

            // 保持滚动位置
            coroutineScope.launch {
                listState.scrollToItem(loadRange)
            }
        }

        // Load future weeks when scrolling down
        if (backwardExpend) {
            if (listState.firstVisibleItemIndex == weekRows.size - visibleRange) {
                // 批量生成后几周的数据
                val futureWeeks = (1..loadRange).map { offset ->
                    generateWeekRow(
                        date = startDate.plusWeeks(weekRows.size.toLong() + offset),
                        startFromSunday = startFromSunday,
                        markers = markers
                    )
                }
                weekRows.addAll(futureWeeks)

                coroutineScope.launch {
                    listState.animateScrollToItem(listState.firstVisibleItemIndex + 1)
                }
            }
        }
    }

}


@Composable
private fun WeekRowItem(
    weekRow: WeekRow,
    weekRowHeight: Dp,
    onMenuItemClick: (Nife) -> Unit,
    setActiveColor: (Int) -> Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = weekRow.monthName,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        weekRow.days.forEach { dayCell ->
            DayCellItem(
                dayCell = dayCell,
                weekRowHeight = weekRowHeight,
                onMenuItemClick = onMenuItemClick,
                setActiveColor = setActiveColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}


@Composable
private fun DayCellItem(
    dayCell: DayCell,
    weekRowHeight: Dp,
    onMenuItemClick: (Nife) -> Unit,
    setActiveColor: (Int) -> Color,
    modifier: Modifier = Modifier
) {
    var expended by remember { mutableStateOf(false) }
    val activeColor: Color = setActiveColor(dayCell.events.size)

    Box(
        modifier = modifier
            .height(weekRowHeight)
            .padding(2.dp)
    ) {
        // 按钮
        Button(
            onClick = { expended = !expended },
            // 圆角矩形
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(0.dp),
            colors = ButtonColors(
                containerColor = activeColor,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = activeColor,
                disabledContentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Text(
                text = dayCell.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        // 下拉菜单
        DropdownMenu(
            expanded = expended,
            onDismissRequest = { expended = false }
        ) {
            dayCell.events.forEach { nife ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = nife.title,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    onClick = {
                        expended = false
                        onMenuItemClick(nife)
                    }
                )
            }
        }
    }


}

private fun generateWeekRow(
    date: LocalDate,
    startFromSunday: Boolean,
    markers: Map<LocalDate, List<Nife>> = emptyMap()
): WeekRow {

    // 确定周的起始日
    val firstDayOfWeek = if (startFromSunday) DayOfWeek.SUNDAY else DayOfWeek.MONDAY

    // 找到本周的第一个日期（包含或早于目标日期的第一个起始日）
    val firstDate = date.with(TemporalAdjusters.previousOrSame(firstDayOfWeek))

    // 生成连续的7天日期
    val week = (0..6).map { offset ->
        firstDate.plusDays(offset.toLong())
    }

    // 检查是否包含月份的第1天
    val firstOfMonth = week.find { it.dayOfMonth == 1 }
    val isStartOfMonth = firstOfMonth != null
    val monthName = if (isStartOfMonth) {
        firstOfMonth?.month?.getDisplayName(TextStyle.SHORT_STANDALONE, Locale.getDefault())
    } else ""

    val now = LocalDate.now()
    val dayCells = week.map { ld ->
        DayCell(date = ld, events = markers[ld] ?: emptyList(), isToday = ld == now)
    }

    return WeekRow(
        days = dayCells,
        isStartOfMonth = isStartOfMonth,
        monthName = monthName ?: ""
    )
}

@Preview(showBackground = true)
@Composable
fun DayOfWeekHeaderPreview() {
    DayOfWeekHeader(startFromSunday = false)
}

@Preview(showBackground = true)
@Composable
fun ACmyPreview() {
    val markers = mapOf(
        LocalDate.now() to listOf(
            Nife(
                title = "Event 1",
                description = "Description 1"
            ),
            Nife(
                title = "Event 2",
                description = "Description 2"
            )
        ),
        LocalDate.now().plusDays(-1) to listOf(
            Nife(
                title = "Event 3",
                description = "Description 3"
            )
        )
    )
    ScrollableEventCalendar(markers = markers, modifier = Modifier.width(300.dp))
}