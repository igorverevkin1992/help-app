package com.helpapp.therapy.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.min

data class PieSegment(
    val label: String,
    val percent: Int,
    val color: Color,
    val tooltip: String,
)

/**
 * Flat 2D pie chart — no 3D, no gradients. Tapping a segment triggers the
 * provided callback with the segment's index so the host screen can render
 * the tooltip (the UX pattern called out in the architectural spec).
 */
@Composable
fun ResponsibilityPie(
    segments: List<PieSegment>,
    selectedIndex: Int?,
    onSegmentSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val total = segments.sumOf { it.percent }.coerceAtLeast(1)
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    Box(
        modifier
            .fillMaxWidth()
            .height(260.dp),
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .pointerInput(segments) {
                    detectTapGestures { tapOffset ->
                        val idx = hitTest(tapOffset, canvasSize, segments, total)
                        if (idx != null) onSegmentSelected(idx)
                    }
                },
        ) {
            canvasSize = size
            val diameter = min(size.width, size.height)
            val topLeftX = (size.width - diameter) / 2f
            val topLeftY = (size.height - diameter) / 2f
            var startAngle = -90f
            segments.forEachIndexed { idx, seg ->
                val sweep = (seg.percent.toFloat() / total) * 360f
                val expand = if (idx == selectedIndex) 8f else 0f
                drawArc(
                    color = seg.color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = true,
                    topLeft = Offset(topLeftX - expand, topLeftY - expand),
                    size = Size(diameter + expand * 2, diameter + expand * 2),
                )
                drawArc(
                    color = Color.Black.copy(alpha = 0.35f),
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = true,
                    topLeft = Offset(topLeftX - expand, topLeftY - expand),
                    size = Size(diameter + expand * 2, diameter + expand * 2),
                    style = Stroke(width = 1.5f),
                )
                startAngle += sweep
            }
        }
    }
}

private fun hitTest(
    tap: Offset,
    canvasSize: Size,
    segments: List<PieSegment>,
    total: Int,
): Int? {
    if (canvasSize == Size.Zero) return null
    val center = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
    val dx = tap.x - center.x
    val dy = tap.y - center.y
    val radius = min(canvasSize.width, canvasSize.height) / 2f
    if (dx * dx + dy * dy > radius * radius) return null
    var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
    angle = (angle + 90f + 360f) % 360f
    var cursor = 0f
    segments.forEachIndexed { idx, seg ->
        val sweep = (seg.percent.toFloat() / total) * 360f
        if (angle >= cursor && angle < cursor + sweep) return idx
        cursor += sweep
    }
    return null
}
