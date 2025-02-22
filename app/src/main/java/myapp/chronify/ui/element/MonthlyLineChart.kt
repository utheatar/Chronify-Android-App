package myapp.chronify.ui.element

import android.graphics.Paint
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import myapp.chronify.data.nife.MonthCount
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt


@Composable
fun MonthlyLineChart(
    data: List<MonthCount>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary
) {
    if (data.isEmpty()) return

    // 找出最大计数值，用于计算比例
    val maxCount = data.maxOf { it.count }

    // 画布配置
    val density = LocalDensity.current
    val textPaint = remember {
        Paint().apply {
            color = android.graphics.Color.GRAY
            textAlign = Paint.Align.CENTER
            textSize = density.run { 12.sp.toPx() }
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val padding = 40f

        val effectiveWidth = canvasWidth - padding * 2
        val effectiveHeight = canvasHeight - padding * 2

        // 绘制x轴
        drawLine(
            color = Color.Gray,
            start = Offset(padding, canvasHeight - padding),
            end = Offset(canvasWidth - padding, canvasHeight - padding),
            strokeWidth = 4f
        )
        // 绘制y轴
        drawLine(
            color = Color.Gray,
            start = Offset(padding, padding),
            end = Offset(padding, canvasHeight - padding),
            strokeWidth = 4f
        )

        // 如果数据点大于1，绘制折线
        if (data.size > 1) {
            val points = data.mapIndexed { index, monthCount ->
                val x = padding + (index * effectiveWidth / (data.size - 1))
                val y = canvasHeight - padding - (monthCount.count * effectiveHeight / maxCount)
                Offset(x, y)
            }

            // 绘制折线
            for (i in 0 until points.size - 1) {
                drawLine(
                    color = lineColor,
                    start = points[i],
                    end = points[i + 1],
                    strokeWidth = 3f
                )
            }

            // 绘制数据点
            points.forEach { point ->
                drawCircle(
                    color = lineColor,
                    radius = 6f,
                    center = point
                )
            }
        }

        // 绘制月份标签
        data.forEachIndexed { index, monthCount ->
            val x = padding + (index * effectiveWidth / (data.size - 1))
            drawContext.canvas.nativeCanvas.drawText(
                monthCount.month,
                x,
                canvasHeight - padding + 25,
                textPaint
            )
        }

        // 绘制计数值标签
        val yAxisLabels = 5
        for (i in 0..yAxisLabels) {
            val value = (maxCount * i / yAxisLabels)
            val y = canvasHeight - padding - (i * effectiveHeight / yAxisLabels)
            drawContext.canvas.nativeCanvas.drawText(
                value.toString(),
                padding - 25,
                y + 5,
                textPaint
            )
        }
    }
}


@Preview
@Composable
fun PreviewChart() {
    val mockData = listOf(
        MonthCount("2024-01", 120),
        MonthCount("2024-02", 230),
        MonthCount("2024-03", 180),
        MonthCount("2024-04", 320)
    )
    MonthlyLineChart(
        data = mockData,
        // chartTitle = "用户活跃度统计"
    )

}

@Composable
fun ScrollableHistogram(
    title: @Composable () -> Unit = {},
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    data: List<MonthCount>,
    barHeight: Int = 300,
    barWidth: Dp = 60.dp,
    barSpacing: Dp = 5.dp,
    barTextHeight: Dp = 16.dp,
    barColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
    averageLineColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
    dataTextColor: Color = Color.White,
    dataTextBackgroundColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {

    if (data.isEmpty()) return

    var selectedBar by remember { mutableStateOf<MonthCount?>(null) }

    // 计算每个柱形需要的最小宽度(包括间距)
    val totalWidthNeeded = (barWidth + barSpacing) * data.size

    val maxCount = data.maxOf { it.count }
    val averageCount = data.sumOf { it.count } / data.size

    Column(
        horizontalAlignment = horizontalAlignment,
        modifier = modifier
    ) {
        title()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barTextHeight + barHeight.dp + barTextHeight)
        ) {
            // 平均值线
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .offset(y = barTextHeight + (barHeight * (1 - averageCount.toFloat() / maxCount)).roundToInt().dp)
                    .background(color = averageLineColor)
            ) {}
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .width(totalWidthNeeded)
                    .height(barTextHeight + barHeight.dp + barTextHeight)
                    .padding(horizontal = 16.dp)
            ) {
                data.forEach { monthData ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier
                            .width(barWidth)
                            .height(barTextHeight + barHeight.dp + barTextHeight)
                    ) {
                        // 如果当前柱形被选中，显示数值
                        if (selectedBar == monthData) {
                            Text(
                                text = monthData.count.toString(),
                                textAlign = TextAlign.Center,
                                color = dataTextColor,
                                modifier = Modifier
                                    .offset(y = (-4).dp)
                                    .background(
                                        color = dataTextBackgroundColor,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 4.dp)
                                    .height(barTextHeight)
                            )
                        }
                        // 柱形区域，高度根据数值按比例计算
                        val renderHeight =
                            if ((barHeight * monthData.count.toFloat() / maxCount) > barHeight) barHeight
                            else (barHeight * monthData.count.toFloat() / maxCount).roundToInt()
                        Box(
                            modifier = Modifier
                                .width(barWidth - barSpacing)
                                .height(renderHeight.dp)
                                .background(
                                    color = barColor,
                                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                )
                                // 添加触摸事件
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onPress = {
                                            selectedBar = monthData
                                            awaitRelease()
                                            selectedBar = null
                                        }
                                    )
                                }
                        ) {}

                        // 月份标签
                        Text(
                            text = monthData.month,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .height(barTextHeight)
                        )
                    }
                }
            }
        }
    }

}

@Preview
@Composable
fun ScrollableHistogramPreview() {
    val sampleData = List(24) { index ->
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, index - 23) // 生成近24个月的数据
        val month = SimpleDateFormat("yy-MM", Locale.getDefault()).format(calendar.time)
        MonthCount(month, (Math.random() * 100).toInt())
    }

    ScrollableHistogram(
        data = sampleData,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}
