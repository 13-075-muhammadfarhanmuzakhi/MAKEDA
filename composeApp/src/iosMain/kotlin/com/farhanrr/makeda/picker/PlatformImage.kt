package com.farhanrr.makeda.picker

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun PlatformAvatarImage(path: String, modifier: Modifier) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier.background(MaterialTheme.colorScheme.primaryContainer)
    )
}