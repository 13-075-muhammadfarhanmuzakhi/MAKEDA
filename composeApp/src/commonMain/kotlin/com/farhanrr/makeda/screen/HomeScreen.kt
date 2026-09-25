package com.farhanrr.makeda.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.farhanrr.makeda.model.Transaction
import com.farhanrr.makeda.model.TransactionCategory
import com.farhanrr.makeda.model.TransactionType
import com.farhanrr.makeda.theme.MakedaExpense
import com.farhanrr.makeda.theme.MakedaIncome
import com.farhanrr.makeda.theme.MakedaPrimary
import com.farhanrr.makeda.theme.MakedaSecondary
import com.farhanrr.makeda.util.formatRupiah
import com.farhanrr.makeda.viewmodel.MakedaViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

private enum class TxFilter(val label: String) { ALL("Semua"), INCOME("Pemasukan"), EXPENSE("Pengeluaran") }

@Composable
fun HomeScreen(viewModel: MakedaViewModel) {
    val state by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf<TransactionType?>(null) }
    var editingTx by remember { mutableStateOf<Transaction?>(null) }
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(TxFilter.ALL) }

    val filteredList = state.transactions
        .filter { filter == TxFilter.ALL || (filter == TxFilter.INCOME && it.type == TransactionType.INCOME) || (filter == TxFilter.EXPENSE && it.type == TransactionType.EXPENSE) }
        .filter { query.isBlank() || it.title.contains(query, ignoreCase = true) || it.displayCategory.contains(query, ignoreCase = true) }

    val budget = state.profile.monthlyBudgetTarget
    val budgetProgress = if (budget > 0) (state.totalPengeluaran.toFloat() / budget.toFloat()).coerceIn(0f, 1f) else 0f

    Scaffold(
        floatingActionButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FloatingActionButton(onClick = { showAddDialog = TransactionType.EXPENSE }, containerColor = MakedaExpense) {
                    Text("-", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                }
                FloatingActionButton(onClick = { showAddDialog = TransactionType.INCOME }, containerColor = MakedaIncome) {
                    Text("+", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = padding.calculateBottomPadding() + 90.dp)
        ) {
            item {
                Text(text = "Halo, ${state.profile.name.substringBefore(' ')} \uD83D\uDC4B", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "MAKEDA", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.height(18.dp))
            }

            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(MakedaPrimary, MakedaSecondary))).padding(22.dp)) {
                        Text(text = "Saldo Saat Ini", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.85f))
                        Text(text = formatRupiah(state.saldo), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(text = "Pemasukan", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.8f))
                                Text(text = formatRupiah(state.totalPemasukan), color = Color.White, fontWeight = FontWeight.SemiBold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "Pengeluaran", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.8f))
                                Text(text = formatRupiah(state.totalPengeluaran), color = Color.White, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Budget Bulan Ini", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                            Text(text = "${(budgetProgress * 100).toInt()}%", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = if (budgetProgress >= 1f) MakedaExpense else MakedaPrimary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { budgetProgress },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(8.dp)),
                            color = if (budgetProgress >= 1f) MakedaExpense else MakedaPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "${formatRupiah(state.totalPengeluaran)} dari ${formatRupiah(budget)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                OutlinedTextField(
                    value = query, onValueChange = { query = it },
                    placeholder = { Text("Cari transaksi...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true, shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(TxFilter.entries.toList()) { f ->
                        FilterChip(
                            selected = filter == f, onClick = { filter = f }, label = { Text(f.label) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MakedaPrimary.copy(alpha = 0.15f))
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Text(text = "Riwayat Transaksi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (filteredList.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Text(
                            text = if (state.transactions.isEmpty()) "Belum ada transaksi. Tekan tombol + atau - untuk menambah." else "Gak ada transaksi yang cocok.",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            items(filteredList, key = { it.id }) { tx ->
                TransactionRow(tx = tx, onEdit = { editingTx = tx }, onDelete = { viewModel.removeTransaction(tx.id) })
                Divider()
            }
        }
    }

    showAddDialog?.let { type ->
        AddTransactionDialog(
            type = type, initial = null,
            onDismiss = { showAddDialog = null },
            onConfirm = { title, amount, category, customLabel, date, hour, minute ->
                viewModel.addTransaction(date = date, title = title, amount = amount, type = type, category = category, customLabel = customLabel, hour = hour, minute = minute)
                showAddDialog = null
            }
        )
    }

    editingTx?.let { tx ->
        AddTransactionDialog(
            type = tx.type, initial = tx,
            onDismiss = { editingTx = null },
            onConfirm = { title, amount, category, customLabel, date, hour, minute ->
                viewModel.updateTransaction(id = tx.id, date = date, title = title, amount = amount, type = tx.type, category = category, customLabel = customLabel, hour = hour, minute = minute)
                editingTx = null
            }
        )
    }
}

@Composable
private fun TransactionRow(tx: Transaction, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onEdit() }.padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape)
                .background(if (tx.type == TransactionType.INCOME) MakedaIncome.copy(alpha = 0.15f) else MakedaExpense.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = if (tx.type == TransactionType.INCOME) "+" else "-", fontWeight = FontWeight.Bold, color = if (tx.type == TransactionType.INCOME) MakedaIncome else MakedaExpense)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = tx.title, fontWeight = FontWeight.Medium)
            val timeSuffix = tx.displayTime?.let { " \u2022 $it" } ?: ""
            Text(text = "${tx.displayCategory} \u2022 ${tx.date}$timeSuffix", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            text = (if (tx.type == TransactionType.INCOME) "+ " else "- ") + formatRupiah(tx.amount),
            color = if (tx.type == TransactionType.INCOME) MakedaIncome else MakedaExpense, fontWeight = FontWeight.SemiBold
        )
        IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "Ubah", tint = MaterialTheme.colorScheme.primary) }
        IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error) }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun AddTransactionDialog(
    type: TransactionType,
    initial: Transaction?,
    onDismiss: () -> Unit,
    onConfirm: (String, Long, TransactionCategory, String?, LocalDate, Int?, Int?) -> Unit
) {
    val accent = if (type == TransactionType.INCOME) MakedaIncome else MakedaExpense
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var amountText by remember { mutableStateOf(initial?.amount?.toString() ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: TransactionCategory.LAINNYA) }
    var customLabel by remember { mutableStateOf(initial?.customLabel ?: "") }
    var expanded by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(initial?.date ?: Clock.System.todayIn(TimeZone.currentSystemDefault())) }
    var showDatePicker by remember { mutableStateOf(false) }
    var useTime by remember { mutableStateOf(initial?.hour != null) }
    var selHour by remember { mutableStateOf(initial?.hour ?: 12) }
    var selMinute by remember { mutableStateOf(initial?.minute ?: 0) }
    var showTimePicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header banner
                Row(
                    modifier = Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(accent, accent.copy(alpha = 0.7f)))).padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (type == TransactionType.INCOME) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                            contentDescription = null, tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (initial != null) "Ubah Transaksi" else if (type == TransactionType.INCOME) "Tambah Pemasukan" else "Tambah Pengeluaran",
                            color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium
                        )
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, contentDescription = "Tutup", tint = Color.White) }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Judul") }, singleLine = true, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { input -> if (input.all { it.isDigit() }) amountText = input },
                        label = { Text("Jumlah (Rp)") }, singleLine = true, shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = selectedDate.toString(), onValueChange = {}, readOnly = true,
                            label = { Text("Tanggal") },
                            trailingIcon = { IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Filled.CalendarMonth, contentDescription = null) } },
                            shape = RoundedCornerShape(14.dp), modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Tambahkan jam? (opsional)", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = useTime, onCheckedChange = { useTime = it })
                    }
                    if (useTime) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = "%02d:%02d".format(selHour, selMinute), onValueChange = {}, readOnly = true,
                            label = { Text("Jam") },
                            trailingIcon = { IconButton(onClick = { showTimePicker = true }) { Icon(Icons.Filled.AccessTime, contentDescription = null) } },
                            shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                        OutlinedTextField(
                            value = category.label, onValueChange = {}, readOnly = true, label = { Text("Kategori") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            TransactionCategory.entries.forEach { cat ->
                                DropdownMenuItem(text = { Text(cat.label) }, onClick = { category = cat; expanded = false })
                            }
                        }
                    }
                    if (category == TransactionCategory.LAINNYA) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = customLabel, onValueChange = { customLabel = it },
                            label = { Text("Nama kategori sendiri (opsional)") },
                            placeholder = { Text("mis. Donasi, Investasi, dll") },
                            singleLine = true, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = accent),
                        shape = RoundedCornerShape(16.dp),
                        onClick = {
                            val amount = amountText.toLongOrNull() ?: 0L
                            if (title.isNotBlank() && amount > 0) {
                                onConfirm(
                                    title.trim(), amount, category,
                                    customLabel.trim().ifBlank { null },
                                    selectedDate,
                                    if (useTime) selHour else null,
                                    if (useTime) selMinute else null
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) { Text("Simpan", fontWeight = FontWeight.SemiBold) }
                    Spacer(modifier = Modifier.height(6.dp))
                    TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Batal") }
                }
            }
        }
    }

    if (showDatePicker) {
        val initialMillis = LocalDateTime(selectedDate, LocalTime(0, 0)).toInstant(TimeZone.UTC).toEpochMilliseconds()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Batal") } }
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        val timeState = rememberTimePickerState(initialHour = selHour, initialMinute = selMinute, is24Hour = true)
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Card(shape = RoundedCornerShape(20.dp)) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    TimePicker(state = timeState)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { showTimePicker = false }) { Text("Batal") }
                        TextButton(onClick = {
                            selHour = timeState.hour; selMinute = timeState.minute
                            showTimePicker = false
                        }) { Text("OK") }
                    }
                }
            }
        }
    }
}

