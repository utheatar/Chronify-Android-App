package myapp.chronify.ui.element.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch


@Composable
fun IntListPicker(
    valueList: List<Int>,
    initialSelectedIndex: Int = 0,
    onItemSelected: (Int, Int) -> Unit = { _, _ -> },
    itemHeight: Dp = 40.dp,
    titleStr: String = "Int Picker",
    showTitle: Boolean = true,
    visibleRadius: Int = 1,
    modifier: Modifier = Modifier
) {

    // 协程作用域，用于控制滚动
    val coroutineScope = rememberCoroutineScope()

    // 重复列表至三倍，用于循环滚动
    val infiniteList = (valueList + valueList + valueList)

    // 列表状态
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = valueList.size + initialSelectedIndex - visibleRadius
    )

    // 选中项的索引
    var selectedIndex by remember { mutableIntStateOf(valueList.size + initialSelectedIndex) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        if (showTitle) {
            Text(
                text = titleStr,
                style = MaterialTheme.typography.titleMedium
            )
        }
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .height(((visibleRadius * 2 + 1) * (itemHeight.value.toInt())).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(infiniteList.size) { index ->
                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .wrapContentWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${infiniteList[index]}",
                        fontSize = if (index == selectedIndex) 20.sp else 16.sp,
                        fontWeight = if (index == selectedIndex) FontWeight.Bold else FontWeight.Normal,
                        // color = if (index == selectedIndex) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
        }

        // 监听滚动状态
        LaunchedEffect(listState.firstVisibleItemIndex) {
            val centerIndex = listState.firstVisibleItemIndex + visibleRadius
            if (centerIndex != selectedIndex) {
                selectedIndex = centerIndex
                onItemSelected(selectedIndex, infiniteList[selectedIndex])
            }
        }

        // 监听滚动停止，自动对齐到中心；实现滚动循环
        LaunchedEffect(listState.isScrollInProgress) {
            if (!listState.isScrollInProgress) {
                // 计算需要滚动到的位置
                val firstVisibleItemIndex = listState.firstVisibleItemIndex
                val visibleItemsInfo = listState.layoutInfo.visibleItemsInfo
                if (visibleItemsInfo.isNotEmpty()) {
                    val firstVisibleItemOffset = visibleItemsInfo[0].offset
                    val firstVisibleItemHeight = visibleItemsInfo[0].size

                    // 计算最接近中心的项
                    val centerOffset = -firstVisibleItemOffset.toFloat() / firstVisibleItemHeight
                    val targetIndex = firstVisibleItemIndex + centerOffset.toInt() + visibleRadius

                    // 平滑滚动到目标位置
                    coroutineScope.launch {
                        listState.animateScrollToItem(targetIndex - visibleRadius)
                    }

                    // 处理循环
                    if (targetIndex < valueList.size) {
                        coroutineScope.launch {
                            listState.scrollToItem(targetIndex + valueList.size - visibleRadius)
                        }
                    } else if (targetIndex >= valueList.size * 2) {
                        coroutineScope.launch {
                            listState.scrollToItem(targetIndex - valueList.size - visibleRadius)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun IntPickerDemo() {
    var selectedMonth by remember { mutableStateOf(1) }
    // 月份列表
    val months = (1..12).toList()
    Column {
        Text("Selected Month: $selectedMonth ")
        IntListPicker(
            months,
            initialSelectedIndex = selectedMonth,
            onItemSelected = { _, month ->
                selectedMonth = month
            },
            visibleRadius = 0,
            modifier = Modifier.width(300.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun YearMonthPickerDemo() {
    var selectedYear by remember { mutableStateOf(2022) }
    var selectedMonth by remember { mutableStateOf(1) }
    // 年份列表
    val years = (2000..2030).toList()
    // 月份列表
    val months = (1..12).toList()
    Column {
        Text("Selected Year: $selectedYear ")
        Text("Selected Month: $selectedMonth ")
        Row {
            IntListPicker(
                years,
                initialSelectedIndex = selectedYear - years[0],
                onItemSelected = { _, year ->
                    selectedYear = year
                },
                modifier = Modifier.width(150.dp)
            )
            VerticalDivider()
            IntListPicker(
                months,
                initialSelectedIndex = selectedMonth - 1,
                onItemSelected = { _, month ->
                    selectedMonth = month
                },
                modifier = Modifier.width(150.dp)
            )
        }
    }
}
