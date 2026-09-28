package com.farhanrr.makeda.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.farhanrr.makeda.theme.MakedaPrimary
import com.farhanrr.makeda.theme.MakedaSecondary
import kotlinx.coroutines.launch

private data class OnboardPage(val icon: ImageVector, val title: String, val desc: String)

private val PAGES = listOf(
    OnboardPage(
        Icons.Filled.AccountBalanceWallet,
        "Catat Setiap Rupiah",
        "Catat pemasukan dan pengeluaran dalam hitungan detik, lengkap dengan kategori dan waktunya."
    ),
    OnboardPage(
        Icons.Filled.Savings,
        "Atur Budget per Kategori",
        "Tetapkan batas untuk makanan, transport, dan lainnya. MAKEDA mengingatkanmu sebelum kebablasan."
    ),
    OnboardPage(
        Icons.Filled.PieChart,
        "Pantau Lewat Grafik",
        "Lihat ke mana uangmu pergi lewat diagram dan tren bulanan, ditambah pengingat menabung tiap malam."
    )
)

@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { PAGES.size })
    val scope = rememberCoroutineScope()
    val isLast = pagerState.currentPage == PAGES.size - 1

    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 24.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onDone) { Text(if (isLast) "" else "Lewati") }
        }

        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f).fillMaxWidth()) { index ->
            val page = PAGES[index]
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .background(Brush.linearGradient(listOf(MakedaPrimary, MakedaSecondary)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(page.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(68.dp))
                }
                Spacer(modifier = Modifier.height(36.dp))
                Text(
                    text = page.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = page.desc,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            PAGES.indices.forEach { i ->
                val active = i == pagerState.currentPage
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(width = if (active) 24.dp else 8.dp, height = 8.dp)
                        .background(
                            if (active) MakedaPrimary else MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(8.dp)
                        )
                )
            }
        }

        Button(
            modifier = Modifier.fillMaxWidth().height(52.dp).padding(bottom = 0.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MakedaPrimary),
            onClick = {
                if (isLast) onDone()
                else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
            }
        ) { Text(if (isLast) "Mulai Sekarang" else "Lanjut", fontWeight = FontWeight.SemiBold) }
        Spacer(modifier = Modifier.height(16.dp))
    }
}