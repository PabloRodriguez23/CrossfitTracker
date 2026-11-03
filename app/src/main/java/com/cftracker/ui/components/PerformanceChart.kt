package com.cftracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/** Línea simple con un punto por valor (en orden cronológico). */
@Composable
fun PerformanceChart(values: List<Double>, modifier: Modifier = Modifier) {
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    Canvas(modifier) {
        if (values.size < 2) return@Canvas
        val pad = 8.dp.toPx()
        val min = values.min()
        val range = (values.max() - min).takeIf { it > 0 } ?: 1.0
        val usableHeight = size.height - 2 * pad
        val stepX = (size.width - 2 * pad) / (values.size - 1)
        val points = values.mapIndexed { i, v ->
            Offset(pad + i * stepX, pad + usableHeight - ((v - min) / range * usableHeight).toFloat())
        }

        listOf(pad, size.height / 2, size.height - pad).forEach { y ->
            drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
        }
        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(path, lineColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        points.forEach { drawCircle(lineColor, radius = 4.dp.toPx(), center = it) }
    }
}
