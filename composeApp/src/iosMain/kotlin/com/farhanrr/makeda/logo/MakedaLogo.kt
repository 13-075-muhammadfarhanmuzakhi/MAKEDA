package com.farhanrr.makeda.logo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
actual fun MakedaLogo(modifier: Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Icon(Icons.Filled.AccountBalanceWallet, contentDescription = "MAKEDA", tint = Color.White, modifier = Modifier.size(46.dp))
    }
}
