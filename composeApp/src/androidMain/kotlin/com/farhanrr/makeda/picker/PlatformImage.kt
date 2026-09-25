package com.farhanrr.makeda.picker

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
actual fun PlatformAvatarImage(path: String, modifier: Modifier) {
    AsyncImage(
        model = path,
        contentDescription = "Foto profil",
        modifier = modifier
    )
}