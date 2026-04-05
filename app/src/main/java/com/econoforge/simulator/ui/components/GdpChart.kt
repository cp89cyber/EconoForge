package com.econoforge.simulator.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.econoforge.simulator.simulation.GdpPoint
import kotlin.math.roundToInt

@Composable
fun GdpChart(
    history: List<GdpPoint>,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .height(280.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.14f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.12f),
                    ),
                ),
            )
            .padding(16.dp)
            .testTag("gdpChart"),
    ) {
        val onSurfaceColor = MaterialTheme.colorScheme.onSurface
        val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
        val surfaceColor = MaterialTheme.colorScheme.surface
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        val metrics = remember(history, widthPx, heightPx) {
            ChartMetrics.from(history, widthPx, heightPx)
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                color = onSurfaceColor.copy(alpha = 0.05f),
                size = size,
            )

            val horizontalGrid = 4
            repeat(horizontalGrid) { index ->
                val y = size.height * index / (horizontalGrid - 1)
                drawLine(
                    color = onSurfaceColor.copy(alpha = 0.08f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx(),
                )
            }

            val labelPaint = Paint().apply {
                color = onSurfaceVariantColor.copy(alpha = 0.85f).toArgb()
                textSize = 12.dp.toPx()
                isAntiAlias = true
            }
            drawContext.canvas.nativeCanvas.drawText(
                "Month ${history.lastOrNull()?.month ?: 0}",
                0f,
                size.height - 8.dp.toPx(),
                labelPaint,
            )
            drawContext.canvas.nativeCanvas.drawText(
                "GDP",
                0f,
                14.dp.toPx(),
                labelPaint,
            )

            if (metrics.points.size > 1) {
                val fillPath = Path().apply {
                    moveTo(metrics.points.first().x, size.height)
                    metrics.points.forEach { point ->
                        lineTo(point.x, point.y)
                    }
                    lineTo(metrics.points.last().x, size.height)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.35f),
                            accentColor.copy(alpha = 0f),
                        ),
                    ),
                )
            }

            if (metrics.points.isNotEmpty()) {
                val path = Path().apply {
                    moveTo(metrics.points.first().x, metrics.points.first().y)
                    metrics.points.drop(1).forEach { point ->
                        lineTo(point.x, point.y)
                    }
                }
                drawPath(
                    path = path,
                    color = accentColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
                drawCircle(
                    color = accentColor,
                    radius = 5.dp.toPx(),
                    center = metrics.points.last(),
                )
            }
        }

        metrics.latestOffset?.let { latestOffset ->
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset {
                        IntOffset(
                            x = (latestOffset.x - widthPx * 0.18f)
                                .roundToInt()
                                .coerceIn(0, (widthPx - widthPx * 0.32f).roundToInt()),
                            y = (latestOffset.y - 40.dp.toPx())
                                .roundToInt()
                                .coerceIn(0, maxOf(0, (heightPx - 40.dp.toPx()).roundToInt())),
                        )
                    },
                color = surfaceColor.copy(alpha = 0.9f),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Text(
                    text = "GDP ${"%.1f".format(metrics.latestValue)}",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = accentColor,
                )
            }
        }
    }
}

private data class ChartMetrics(
    val points: List<Offset>,
    val latestOffset: Offset?,
    val latestValue: Float,
) {
    companion object {
        fun from(history: List<GdpPoint>, widthPx: Float, heightPx: Float): ChartMetrics {
            if (history.isEmpty() || widthPx <= 0f || heightPx <= 0f) {
                return ChartMetrics(emptyList(), null, 0f)
            }
            val leftPadding = 10f
            val rightPadding = 22f
            val topPadding = 18f
            val bottomPadding = 28f
            val minValue = history.minOf { it.gdpIndex }.let { it * 0.985f }
            val maxValue = history.maxOf { it.gdpIndex }.let { maxOf(it * 1.015f, minValue + 1f) }
            val usableWidth = widthPx - leftPadding - rightPadding
            val usableHeight = heightPx - topPadding - bottomPadding
            val divisor = maxOf(1, history.lastIndex)
            val points = history.mapIndexed { index, point ->
                val x = leftPadding + usableWidth * (index / divisor.toFloat())
                val normalizedY = (point.gdpIndex - minValue) / (maxValue - minValue)
                val y = topPadding + usableHeight * (1f - normalizedY)
                Offset(x, y)
            }
            return ChartMetrics(
                points = points,
                latestOffset = points.lastOrNull(),
                latestValue = history.last().gdpIndex,
            )
        }
    }
}
