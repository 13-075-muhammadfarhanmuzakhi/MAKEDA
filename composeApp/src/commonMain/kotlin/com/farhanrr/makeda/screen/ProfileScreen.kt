package com.farhanrr.makeda.screen

import com.farhanrr.makeda.util.ThousandsTransformation
import com.farhanrr.makeda.util.digitsOnly
import com.farhanrr.makeda.model.EXPENSE_CATEGORIES
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.farhanrr.makeda.picker.PlatformAvatarImage
import com.farhanrr.makeda.picker.rememberImagePicker
import com.farhanrr.makeda.share.shareText
import com.farhanrr.makeda.util.DOWNLOAD_URL
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Receipt
import com.farhanrr.makeda.model.TransactionCategory
import com.farhanrr.makeda.model.TransactionType
import com.farhanrr.makeda.model.Transaction
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import com.farhanrr.makeda.util.DOWNLOAD_URL
import com.farhanrr.makeda.util.formatRupiah
import com.farhanrr.makeda.viewmodel.MakedaViewModel

@Composable
fun ProfileScreen(viewModel: MakedaViewModel, onLogout: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    var name by remember(state.profile.name) { mutableStateOf(state.profile.name) }
    var email by remember(state.profile.email) { mutableStateOf(state.profile.email) }
    var budgetText by remember(state.profile.monthlyBudgetTarget) { mutableStateOf(state.profile.monthlyBudgetTarget.toString()) }
    var photoPath by remember(state.profile.photoPath) { mutableStateOf(state.profile.photoPath) }

    val catTexts = remember(state.profile.categoryBudgets) {
        mutableStateMapOf<String, String>().apply {
            state.profile.categoryBudgets.forEach { (k, v) -> put(k, v.toString()) }
        }
    }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var goalName by remember(state.profile.goalName) { mutableStateOf(state.profile.goalName) }
    var goalTargetText by remember(state.profile.goalTarget) {
        mutableStateOf(if (state.profile.goalTarget > 0) state.profile.goalTarget.toString() else "")
    }
    val clipboard = LocalClipboardManager.current
    var linkCopied by remember { mutableStateOf(false) }
    var testSent by remember { mutableStateOf(false) }

    val pickImage = rememberImagePicker { result ->
        if (result != null) {
            photoPath = result
            // langsung tersimpan, tidak perlu menekan Simpan Profil
            viewModel.updateProfile(viewModel.uiState.value.profile.copy(photoPath = result))
        }
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text(text = "Profil", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(androidx.compose.ui.graphics.Brush.linearGradient(listOf(com.farhanrr.makeda.theme.MakedaPrimary, com.farhanrr.makeda.theme.MakedaSecondary)))
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(92.dp).clickable { pickImage() }) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(88.dp)
                            .align(Alignment.TopStart)
                            .clip(CircleShape)
                            .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.25f))
                            .border(3.dp, androidx.compose.ui.graphics.Color.White, CircleShape)
                    ) {
                        val currentPhoto = photoPath
                        if (currentPhoto != null) {
                            PlatformAvatarImage(path = currentPhoto, modifier = Modifier.fillMaxSize().clip(CircleShape))
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = androidx.compose.ui.graphics.Color.White,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(androidx.compose.ui.graphics.Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = "Ganti foto",
                            tint = com.farhanrr.makeda.theme.MakedaPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = state.profile.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color.White)
                    Text(text = state.profile.email, style = MaterialTheme.typography.bodyMedium, color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f))
                    Text(text = "Ketuk foto untuk ganti (otomatis bulat)", style = MaterialTheme.typography.bodySmall, color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f))
                }
            }
        }

        if (state.isGuest) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Text(
                    text = "Kamu sedang di Mode Tamu. Data hanya tersimpan di perangkat ini. Tekan Keluar, lalu Daftar untuk membuat akun.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }

        if (photoPath != null) {
            TextButton(
                onClick = {
                    photoPath = null
                    viewModel.updateProfile(viewModel.uiState.value.profile.copy(photoPath = null))
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Hapus foto profil") }
        }

        Spacer(modifier = Modifier.height(16.dp))
        ProfileStatsSection(transactions = state.transactions)

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Pengaturan Akun", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = budgetText,
            onValueChange = { input -> budgetText = digitsOnly(input) },
            label = { Text("Target Anggaran Bulanan (Rp)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = ThousandsTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Target saat ini: ${formatRupiah(budgetText.toLongOrNull() ?: 0L)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Budget per Kategori", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            text = "Kosongkan jika tidak ingin membatasi. Notifikasi muncul saat terpakai 80% dan 100%.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(10.dp))
        EXPENSE_CATEGORIES.forEach { cat ->
            OutlinedTextField(
                value = catTexts[cat.name] ?: "",
                onValueChange = { input -> catTexts[cat.name] = digitsOnly(input) },
                label = { Text("Batas ${cat.label} (Rp)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = ThousandsTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Target Tabungan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = goalName, onValueChange = { goalName = it },
            label = { Text("Nama target (mis. Beli laptop)") },
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = goalTargetText,
            onValueChange = { input -> goalTargetText = digitsOnly(input) },
            label = { Text("Nominal target (Rp)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = ThousandsTransformation(),
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Progres dihitung dari transaksi kategori Tabungan",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Pengingat Menabung", fontWeight = FontWeight.SemiBold)
                    Text(text = "Notifikasi setiap hari pukul 20.00", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = state.settings.dailyReminderEnabled,
                    onCheckedChange = { viewModel.updateSettings(state.settings.copy(dailyReminderEnabled = it)) }
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.sendTestNotification(); testSent = true }
        ) {
            Icon(Icons.Filled.NotificationsActive, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (testSent) "Notifikasi tes dikirim, tunggu 3 detik" else "Tes Notifikasi")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Mode Gelap", fontWeight = FontWeight.SemiBold)
                    Text(text = "Ubah tampilan aplikasi", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = state.settings.darkMode,
                    onCheckedChange = { viewModel.updateSettings(state.settings.copy(darkMode = it)) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                viewModel.updateProfile(
                    state.profile.copy(
                        name = name.ifBlank { state.profile.name },
                        email = email.ifBlank { state.profile.email },
                        monthlyBudgetTarget = budgetText.toLongOrNull() ?: state.profile.monthlyBudgetTarget,
                        goalName = goalName.trim(),
                        goalTarget = goalTargetText.toLongOrNull() ?: 0L,
                        photoPath = photoPath,
                        categoryBudgets = EXPENSE_CATEGORIES.mapNotNull { c ->
                            catTexts[c.name]?.toLongOrNull()?.takeIf { it > 0 }?.let { c.name to it }
                        }.toMap()
                    )
                )
            }
        ) { Text("Simpan Profil") }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                shareText(
                    "MAKEDA",
                    "Coba MAKEDA, aplikasi pencatat keuangan pribadi: catat pemasukan & pengeluaran, pantau budget bulanan, dan export laporan.\n\nDownload di: $DOWNLOAD_URL"
                )
            }
        ) {
            Icon(Icons.Filled.Share, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Bagikan Aplikasi")
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                clipboard.setText(AnnotatedString(DOWNLOAD_URL))
                linkCopied = true
            }
        ) {
            Icon(Icons.Filled.ContentCopy, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (linkCopied) "Link tersalin" else "Salin Link Unduhan")
        }
        Text(
            text = DOWNLOAD_URL,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { shareText("Ekspor data MAKEDA", buildCsv(state.transactions)) }
        ) {
            Icon(Icons.Filled.FileDownload, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Ekspor Data (CSV)")
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { showDeleteConfirm = true }
        ) {
            Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Hapus Semua Transaksi", color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onLogout
        ) {
            Icon(Icons.Filled.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Keluar")
        }

        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Info, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "MAKEDA v3.0  \u2022  $DOWNLOAD_URL",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Hapus semua transaksi?") },
            text = { Text("Semua catatan pemasukan & pengeluaran akun ini akan dihapus permanen.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearAllTransactions()
                    showDeleteConfirm = false
                }) { Text("Hapus", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Batal") } }
        )
    }
}


private val SAVING_TIPS = listOf(
    "Sisihkan tabungan di awal, bukan dari sisa uang jajan.",
    "Coba aturan 50/30/20: 50% kebutuhan, 30% keinginan, 20% tabungan.",
    "Tunda belanja impulsif 24 jam. Kalau masih mau, baru beli.",
    "Menabung receh tiap hari lebih kuat daripada menunggu uang besar.",
    "Catat semua pengeluaran kecil, kopi dan jajan juga ikut dihitung.",
    "Pisahkan uang tabungan dari uang harian supaya tidak tergoda.",
    "Punya target yang jelas bikin menabung lebih semangat."
)

@Composable
private fun ProfileStatsSection(transactions: List<Transaction>) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val dates = transactions.map { it.date }.toSet()
    var streak = 0
    var d: LocalDate = if (today in dates) today else today.minus(1, DateTimeUnit.DAY)
    while (d in dates) {
        streak++
        d = d.minus(1, DateTimeUnit.DAY)
    }
    val totalSaved = transactions.filter { it.category == TransactionCategory.TABUNGAN }.sumOf { it.amount }
    val tip = SAVING_TIPS[today.dayOfYear % SAVING_TIPS.size]

    Text(text = "Ringkasan Kamu", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(10.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatTile(Icons.Filled.Receipt, transactions.size.toString(), "Transaksi", Modifier.weight(1f))
        StatTile(Icons.Filled.LocalFireDepartment, "$streak hari", "Beruntun catat", Modifier.weight(1f))
    }
    Spacer(modifier = Modifier.height(10.dp))
    StatTile(Icons.Filled.Savings, formatRupiah(totalSaved), "Total tabungan", Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(10.dp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Lightbulb, contentDescription = null, tint = com.farhanrr.makeda.theme.MakedaSecondary)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = "Tips menabung hari ini", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Text(text = tip, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun StatTile(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String, modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp)) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = com.farhanrr.makeda.theme.MakedaPrimary)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun csvCell(s: String): String = "\"" + s.replace("\"", "\"\"") + "\""

private fun buildCsv(list: List<Transaction>): String {
    val sb = StringBuilder("Tanggal,Jam,Judul,Kategori,Jenis,Jumlah\n")
    list.sortedBy { it.date }.forEach { t ->
        sb.append(t.date.toString()).append(',')
            .append(t.displayTime ?: "").append(',')
            .append(csvCell(t.title)).append(',')
            .append(csvCell(t.displayCategory)).append(',')
            .append(if (t.type == TransactionType.INCOME) "Pemasukan" else "Pengeluaran").append(',')
            .append(t.amount).append('\n')
    }
    return sb.toString()
}