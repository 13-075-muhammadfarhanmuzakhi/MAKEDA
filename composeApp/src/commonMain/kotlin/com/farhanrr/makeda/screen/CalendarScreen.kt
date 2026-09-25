package com.farhanrr.makeda.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.farhanrr.makeda.model.TransactionType
import com.farhanrr.makeda.theme.MakedaExpense
import com.farhanrr.makeda.theme.MakedaIncome
import com.farhanrr.makeda.util.formatRupiah
import com.farhanrr.makeda.viewmodel.MakedaViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.todayIn

@Composable
fun CalendarScreen(viewModel: MakedaViewModel) {
    val state by viewModel.uiState.collectAsState()
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    var selectedDate by remember { mutableStateOf(today) }
    var showAddReminder by remember { mutableStateOf(false) }

    val year = today.year
    val month = today.month
    val daysInMonth = daysInMonth(year, month)
    val firstDay = LocalDate(year, month, 1)
    val startOffset = firstDay.dayOfWeek.value % 7

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddReminder = true }) {
                Icon(Icons.Filled.NotificationsActive, contentDescription = "Tambah pengingat")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(16.dp).padding(bottom = padding.calculateBottomPadding())) {
            Text(text = "Kalender Keuangan", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(text = "$month $year", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    val dayLabels = listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        dayLabels.forEach {
                            Text(text = it, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.fillMaxWidth()) {
                        items(startOffset) { Box(modifier = Modifier.aspectRatio(1f)) }
                        items(daysInMonth) { dayIndex ->
                            val date = LocalDate(year, month, dayIndex + 1)
                            val hasTx = state.transactions.any { it.date == date }
                            val hasReminder = state.reminders.any { it.date == date }
                            val isSelected = date == selectedDate
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .padding(3.dp)
                                    .clickable { selectedDate = date }
                                    .background(
                                        color = if (isSelected) MaterialTheme.colorScheme.primary
                                                else if (hasTx || hasReminder) MaterialTheme.colorScheme.secondaryContainer
                                                else Color.Transparent,
                                        shape = RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${dayIndex + 1}",
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        if (hasTx) Dot(isSelected)
                                        if (hasReminder) Dot(isSelected)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedContent(
                targetState = selectedDate,
                transitionSpec = { fadeIn(tween(220)).togetherWith(fadeOut(tween(120))) },
                label = "day-detail"
            ) { date ->
                Column {
                    Text(text = "Transaksi pada $date", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    val txForDate = viewModel.transactionsForDate(date)
                    if (txForDate.isEmpty()) {
                        Text(text = "Tidak ada transaksi.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        txForDate.forEach { tx ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "${tx.title} (${tx.category.label})")
                                Text(
                                    text = (if (tx.type == TransactionType.INCOME) "+ " else "- ") + formatRupiah(tx.amount),
                                    color = if (tx.type == TransactionType.INCOME) MakedaIncome else MakedaExpense
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(text = "Pengingat pada $date", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    val remindersForDate = viewModel.remindersForDate(date)
                    if (remindersForDate.isEmpty()) {
                        Text(text = "Tidak ada pengingat.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        remindersForDate.forEach { reminder ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = reminder.title, fontWeight = FontWeight.Medium)
                                    Text(
                                        text = "${reminder.hour.toString().padStart(2, '0')}:${reminder.minute.toString().padStart(2, '0')} \u2022 ${reminder.note}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                IconButton(onClick = { viewModel.removeReminder(reminder.id) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Hapus pengingat", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddReminder) {
        AddReminderDialog(
            date = selectedDate,
            onDismiss = { showAddReminder = false },
            onConfirm = { hour, minute, title, note ->
                viewModel.addReminder(selectedDate, hour, minute, title, note)
                showAddReminder = false
            }
        )
    }
}

@Composable
private fun Dot(selected: Boolean) {
    Box(
        modifier = Modifier
            .height(5.dp)
            .aspectRatio(1f)
            .background(
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                shape = CircleShape
            )
    )
}

@Composable
private fun AddReminderDialog(
    date: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var hourText by remember { mutableStateOf("8") }
    var minuteText by remember { mutableStateOf("0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pengingat untuk $date") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Pengingat") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Catatan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = hourText,
                        onValueChange = { input -> if (input.all { it.isDigit() }) hourText = input },
                        label = { Text("Jam") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = minuteText,
                        onValueChange = { input -> if (input.all { it.isDigit() }) minuteText = input },
                        label = { Text("Menit") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val hour = (hourText.toIntOrNull() ?: 8).coerceIn(0, 23)
                val minute = (minuteText.toIntOrNull() ?: 0).coerceIn(0, 59)
                if (title.isNotBlank()) onConfirm(hour, minute, title.trim(), note.trim())
            }) { Text("Simpan & Jadwalkan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

private fun daysInMonth(year: Int, month: Month): Int {
    val nextMonth = if (month.value == 12) LocalDate(year + 1, 1, 1) else LocalDate(year, month.value + 1, 1)
    val thisMonth = LocalDate(year, month.value, 1)
    return thisMonth.daysUntil(nextMonth)
}