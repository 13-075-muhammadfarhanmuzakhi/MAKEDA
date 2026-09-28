package com.farhanrr.makeda.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay

/** Muncul pelan: fade + naik sedikit. delayMs dipakai untuk efek berurutan (stagger). */
@Composable
fun Modifier.appear(delayMs: Int = 0): Modifier {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(delayMs.toLong())
        progress.animateTo(1f, tween(520, easing = FastOutSlowInEasing))
    }
    return this.graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * 48f
    }
}

/** Angka berubah halus (count-up). Saat animasi selesai selalu mengembalikan nilai persis. */
@Composable
fun animatedLong(target: Long): Long {
    val value by animateFloatAsState(
        targetValue = target.toFloat(),
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "animated-long"
    )
    return if (value == target.toFloat()) target else value.toLong()
}

/** Float berubah halus (progress bar dll). */
@Composable
fun animatedFloat(target: Float): Float {
    val value by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "animated-float"
    )
    return value
}
