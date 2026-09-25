package com.farhanrr.makeda.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.farhanrr.makeda.model.TransactionType
import com.farhanrr.makeda.theme.MakedaExpense
import com.farhanrr.makeda.theme.MakedaIncome
import com.farhanrr.makeda.util.formatRupiah
import com.farhanrr.makeda.viewmodel.MakedaViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@Composable
fun ReportScreen(viewModel: MakedaViewModel) {
    val state by viewModel.uiState.collectAsState()
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    val monthTx = viewModel.transactionsForMonth(today.year, today.monthNumber)

    val income = monthTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val expense = monthTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val net = income - expense

    val byCategory = monthTx.filter { it.type == TransactionType.EXPENSE }
        .groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }

    val maxCategory = byCategory.maxOfOrNull { it.second } ?: 1L

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Laporan Bulan Ini", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(text = "${today.month} ${today.year}", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "Selisih Bersih", style = MaterialTheme.typography.labelLarge)
                Text(
                    text = formatRupiah(net),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (net >= 0) MakedaIncome else MakedaExpense
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(text = "Total Pemasukan", style = MaterialTheme.typography.labelMedium)
                        Text(text = formatRupiah(income), color = MakedaIncome, fontWeight = FontWeight.SemiBold)
                    }
                    Column {
                        Text(text = "Total Pengeluaran", style = MaterialTheme.typography.labelMedium)
                        Text(text = formatRupiah(expense), color = MakedaExpense, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "Pengeluaran per Kategori", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        if (byCategory.isEmpty()) {
            Text(text = "Belum ada pengeluaran bulan ini.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            LazyColumn {
                items(byCategory) { (category, amount) ->
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = category.label, fontWeight = FontWeight.Medium)
                            Text(text = formatRupiah(amount))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { (amount.toFloat() / maxCategory.toFloat()).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
