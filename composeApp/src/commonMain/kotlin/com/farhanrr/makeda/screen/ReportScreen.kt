package com.farhanrr.makeda.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.farhanrr.makeda.model.TransactionType
import com.farhanrr.makeda.share.shareText
import com.farhanrr.makeda.theme.MakedaExpense
import com.farhanrr.makeda.theme.MakedaIncome
import com.farhanrr.makeda.theme.MakedaPrimary
import com.farhanrr.makeda.theme.MakedaSecondary
import com.farhanrr.makeda.util.formatRupiah
import com.farhanrr.makeda.ui.BudgetProgressList
import com.farhanrr.makeda.ui.ChartCard
import com.farhanrr.makeda.ui.DonutChart
import com.farhanrr.makeda.ui.LineChart
import com.farhanrr.makeda.ui.daysInMonth
import com.farhanrr.makeda.viewmodel.MakedaViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

private val MONTH_NAMES = listOf(
    "Januari", "Februari", "Maret", "April", "Mei", "Juni",
    "Juli", "Agustus", "September", "Oktober", "November", "Desember"
)

@Composable
fun ReportScreen(viewModel: MakedaViewModel) {
    val state by viewModel.uiState.collectAsState()
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    var year by remember { mutableStateOf(today.year) }
    var month by remember { mutableStateOf(today.monthNumber) }

    val monthTx = state.transactions.filter { it.date.year == year && it.date.monthNumber == month }
    val income = monthTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val expense = monthTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val net = income - expense

    val expenseByCat = monthTx.filter { it.type == TransactionType.EXPENSE }
        .groupBy { it.displayCategory }
        .map { entry -> Pair(entry.key, entry.value.sumOf { it.amount }) }
        .sortedByDescending { it.second }
    val incomeByCat = monthTx.filter { it.type == TransactionType.INCOME }
        .groupBy { it.displayCategory }
        .map { entry -> Pair(entry.key, entry.value.sumOf { it.amount }) }
        .sortedByDescending { it.second }
    val maxExpense = expenseByCat.maxOfOrNull { it.second } ?: 1L
    val maxIncome = incomeByCat.maxOfOrNull { it.second } ?: 1L
    val savingRate = if (income > 0) ((net * 100) / income).toInt() else 0
    val monthLabel = "${MONTH_NAMES[month - 1]} $year"
    val dailyExpense = List(daysInMonth(year, month)) { d ->
        monthTx.filter { it.type == TransactionType.EXPENSE && it.date.dayOfMonth == d + 1 }.sumOf { it.amount }
    }
    val spentByCat = monthTx.filter { it.type == TransactionType.EXPENSE }
        .groupBy { it.category }.mapValues { e -> e.value.sumOf { it.amount } }
    val budgets = state.profile.categoryBudgets.filterValues { it > 0L }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 32.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Laporan", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Button(
                    onClick = {
                        val report = buildString {
                            appendLine("LAPORAN KEUANGAN MAKEDA")
                            appendLine(monthLabel)
                            appendLine("------------------------------")
                            appendLine("Total Pemasukan   : ${formatRupiah(income)}")
                            appendLine("Total Pengeluaran : ${formatRupiah(expense)}")
                            appendLine("Selisih Bersih    : ${formatRupiah(net)}")
                            appendLine("------------------------------")
                            appendLine("Pemasukan per Kategori:")
                            if (incomeByCat.isEmpty()) appendLine("(kosong)")
                            incomeByCat.forEach { appendLine("- ${it.first}: ${formatRupiah(it.second)}") }
                            appendLine("")
                            appendLine("Pengeluaran per Kategori:")
                            if (expenseByCat.isEmpty()) appendLine("(kosong)")
                            expenseByCat.forEach { appendLine("- ${it.first}: ${formatRupiah(it.second)}") }
                            appendLine("------------------------------")
                            appendLine("Rincian Transaksi:")
                            monthTx.sortedBy { it.date }.forEach { tx ->
                                val sign = if (tx.type == TransactionType.INCOME) "+" else "-"
                                val time = tx.displayTime?.let { " $it" } ?: ""
                                appendLine("${tx.date}$time | ${tx.title} (${tx.displayCategory}) | $sign${formatRupiah(tx.amount)}")
                            }
                        }
                        shareText("Laporan MAKEDA - $monthLabel", report)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MakedaPrimary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.IosShare, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                    Text("Export")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (month == 1) { month = 12; year -= 1 } else { month -= 1 }
                }) { Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Bulan sebelumnya") }
                Text(text = monthLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = {
                    if (month == 12) { month = 1; year += 1 } else { month += 1 }
                }) { Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Bulan berikutnya") }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(MakedaPrimary, MakedaSecondary)))
                        .padding(22.dp)
                ) {
                    Text(text = "Selisih Bersih", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.85f))
                    Text(text = formatRupiah(net), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(text = "Pemasukan", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.8f))
                            Text(text = formatRupiah(income), color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Pengeluaran", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.8f))
                            Text(text = formatRupiah(expense), color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(title = "Transaksi", value = monthTx.size.toString(), modifier = Modifier.weight(1f))
                StatCard(title = "Rasio Tabungan", value = "$savingRate%", modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        item {
            ChartCard(title = "Komposisi Pengeluaran") {
                if (expenseByCat.isEmpty()) {
                    Text("Belum ada pengeluaran di bulan ini.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    DonutChart(expenseByCat)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            ChartCard(title = "Tren Pengeluaran Harian") { LineChart(dailyExpense, MakedaExpense) }
            Spacer(modifier = Modifier.height(12.dp))
            if (budgets.isNotEmpty()) {
                ChartCard(title = "Budget per Kategori") { BudgetProgressList(budgets, spentByCat) }
                Spacer(modifier = Modifier.height(12.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Text(text = "Pengeluaran per Kategori", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            if (expenseByCat.isEmpty()) {
                Text(text = "Belum ada pengeluaran di bulan ini.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        items(expenseByCat) { (name, amount) ->
            CategoryBar(name = name, amount = amount, max = maxExpense, total = expense, color = MakedaExpense)
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "Pemasukan per Kategori", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            if (incomeByCat.isEmpty()) {
                Text(text = "Belum ada pemasukan di bulan ini.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        items(incomeByCat) { (name, amount) ->
            CategoryBar(name = name, amount = amount, max = maxIncome, total = income, color = MakedaIncome)
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(18.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CategoryBar(name: String, amount: Long, max: Long, total: Long, color: Color) {
    val percent = if (total > 0) ((amount * 100) / total).toInt() else 0
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "$name ($percent%)", fontWeight = FontWeight.Medium)
            Text(text = formatRupiah(amount))
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (amount.toFloat() / max.toFloat()).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(8.dp)),
            color = color
        )
    }
}