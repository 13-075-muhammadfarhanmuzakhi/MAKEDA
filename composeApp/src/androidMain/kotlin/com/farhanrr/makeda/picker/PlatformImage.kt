package com.farhanrr.makeda.picker

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

/** Foto selalu bulat & diisi penuh (crop tengah), apa pun rasio aslinya. */
@Composable
actual fun PlatformAvatarImage(path: String, modifier: Modifier) {
    AsyncImage(
        model = path,
        contentDescription = "Foto profil",
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(CircleShape)
    )
}
