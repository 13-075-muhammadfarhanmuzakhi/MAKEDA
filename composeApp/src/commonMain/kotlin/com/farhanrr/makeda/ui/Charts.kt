package com.farhanrr.makeda.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.farhanrr.makeda.model.EXPENSE_CATEGORIES
import com.farhanrr.makeda.model.TransactionCategory
import com.farhanrr.makeda.theme.MakedaExpense
import com.farhanrr.makeda.theme.MakedaPrimary
import com.farhanrr.makeda.util.formatRupiah

val ChartPalette = listOf(
    Color(0xFF3D5AFE), Color(0xFF00BFA5), Color(0xFFFFA000), Color(0xFFD32F2F),
    Color(0xFF8E24AA), Color(0xFF00ACC1), Color(0xFF6D4C41), Color(0xFF7CB342)
)

fun daysInMonth(year: Int, month: Int): Int = when (month) {
    2 -> if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) 29 else 28
    4, 6, 9, 11 -> 30
    else -> 31
}

@Composable
fun ChartCard(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun DonutChart(slices: List<Pair<String, Long>>) {
    val total = slices.sumOf { it.second }.coerceAtLeast(1L)
    val anim = remember { Animatable(0f) }
    LaunchedEffect(slices) {
        anim.snapTo(0f)
        anim.animateTo(1f, tween(800))
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Canvas(modifier = Modifier.size(140.dp)) {
            val stroke = 26.dp.toPx()
            val arcSize = Size(size.width - stroke, size.height - stroke)
            var start = -90f
            slices.forEachIndexed { i, s ->
                val full = s.second.toFloat() / total.toFloat() * 360f
                drawArc(
                    color = ChartPalette[i % ChartPalette.size],
                    startAngle = start,
                    sweepAngle = (full * anim.value - 1f).coerceAtLeast(0f),
                    useCenter = false,
                    topLeft = Offset(stroke / 2f, stroke / 2f),
                    size = arcSize,
                    style = Stroke(width = stroke)
                )
                start += full
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            slices.take(6).forEachIndexed { i, s ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(ChartPalette[i % ChartPalette.size]))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = s.first,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                    Text(
                        text = "${(s.second * 100 / total)}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun LineChart(values: List<Long>, lineColor: Color = MakedaPrimary) {
    val maxV = (values.maxOrNull() ?: 0L).coerceAtLeast(1L)
    val anim = remember { Animatable(0f) }
    LaunchedEffect(values) {
        anim.snapTo(0f)
        anim.animateTo(1f, tween(900))
    }
    Column {
        Text(
            text = "Tertinggi: ${formatRupiah(maxV)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        Canvas(modifier = Modifier.fillMaxWidth().height(140.dp)) {
            val n = values.size
            if (n < 2) return@Canvas
            val w = size.width
            val h = size.height
            val pad = 8.dp.toPx()
            fun px(i: Int) = i.toFloat() / (n - 1).toFloat() * w
            fun py(v: Long) = h - pad - (v.toFloat() / maxV.toFloat()) * (h - pad * 2f) * anim.value
            val line = Path()
            values.forEachIndexed { i, v ->
                if (i == 0) line.moveTo(px(i), py(v)) else line.lineTo(px(i), py(v))
            }
            val fill = Path().apply {
                addPath(line)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(fill, Brush.verticalGradient(listOf(lineColor.copy(alpha = 0.30f), Color.Transparent)))
            drawPath(line, lineColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
            values.forEachIndexed { i, v ->
                if (v > 0L) drawCircle(lineColor, radius = 3.5.dp.toPx(), center = Offset(px(i), py(v)))
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("1", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${values.size / 2}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${values.size}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun BudgetProgressList(budgets: Map<String, Long>, spent: Map<TransactionCategory, Long>) {
    val warn = Color(0xFFFFA000)
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        EXPENSE_CATEGORIES.forEach { cat ->
            val limit = budgets[cat.name] ?: return@forEach
            if (limit <= 0L) return@forEach
            val used = spent[cat] ?: 0L
            val ratio = used.toFloat() / limit.toFloat()
            val color = when {
                ratio >= 1f -> MakedaExpense
                ratio >= 0.8f -> warn
                else -> MakedaPrimary
            }
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = cat.label, fontWeight = FontWeight.Medium)
                    Text(text = "${formatRupiah(used)} / ${formatRupiah(limit)}", style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { ratio.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(8.dp)),
                    color = color
                )
                val note = when {
                    ratio >= 1f -> "Melebihi batas ${formatRupiah(used - limit)}"
                    ratio >= 0.8f -> "Hampir habis, sisa ${formatRupiah(limit - used)}"
                    else -> "Sisa ${formatRupiah(limit - used)}"
                }
                Text(text = note, style = MaterialTheme.typography.labelSmall, color = color)
            }
        }
    }
}