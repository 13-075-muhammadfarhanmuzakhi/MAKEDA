package com.farhanrr.makeda.logo

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.farhanrr.makeda.R

@Composable
actual fun MakedaLogo(modifier: Modifier) {
    Image(
        painter = painterResource(R.drawable.makeda_logo),
        contentDescription = "MAKEDA",
        contentScale = ContentScale.Crop,
        modifier = modifier
    )
}
