package com.helpapp.therapy.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Minimal flat line chart: plots [points] normalized into the canvas.
 * Optionally draws a horizontal reference line at [referenceValue] (e.g. the
 * MIDS cut-off of 27). No grid clutter, no 3D, no fills.
 */
@Composable
fun SparkLine(
    points: List<Float>,
    lineColor: Color,
    referenceValue: Float? = null,
    referenceColor: Color = Color.Gray,
    yMin: Float = 0f,
    yMax: Float = 72f,
    modifier: Modifier = Modifier,
) {
    Canvas(
        modifier
            .fillMaxWidth()
            .height(140.dp),
    ) {
        if (points.isEmpty()) return@Canvas
        val range = (yMax - yMin).coerceAtLeast(1f)
        val stepX = if (points.size > 1) size.width / (points.size - 1) else 0f
        val path = Path()
        points.forEachIndexed { index, raw ->
            val value = raw.coerceIn(yMin, yMax)
            val x = index * stepX
            val y = size.height - ((value - yMin) / range) * size.height
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path = path, color = lineColor, style = Stroke(width = 4f))

        points.forEachIndexed { index, raw ->
            val value = raw.coerceIn(yMin, yMax)
            val x = index * stepX
            val y = size.height - ((value - yMin) / range) * size.height
            drawCircle(color = lineColor, radius = 5f, center = Offset(x, y))
        }

        referenceValue?.let { ref ->
            val clamped = ref.coerceIn(yMin, yMax)
            val y = size.height - ((clamped - yMin) / range) * size.height
            drawLine(
                color = referenceColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f)),
            )
        }
    }
}
