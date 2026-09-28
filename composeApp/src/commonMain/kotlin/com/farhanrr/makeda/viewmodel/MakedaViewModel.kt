package com.farhanrr.makeda.viewmodel

import com.farhanrr.makeda.util.formatRupiah
import androidx.lifecycle.ViewModel
import com.farhanrr.makeda.model.AppSettings
import com.farhanrr.makeda.model.AuthAccount
import com.farhanrr.makeda.model.Reminder
import com.farhanrr.makeda.model.Transaction
import com.farhanrr.makeda.model.TransactionCategory
import com.farhanrr.makeda.model.TransactionType
import com.farhanrr.makeda.model.UserProfile
import com.farhanrr.makeda.reminder.PlatformReminderScheduler
import com.farhanrr.makeda.store.PlatformStore
import com.farhanrr.makeda.util.epochMillisOf
import com.farhanrr.makeda.util.DAILY_REMINDER_HOUR
import com.farhanrr.makeda.util.DAILY_REMINDER_ID
import com.farhanrr.makeda.util.DAILY_REMINDER_MESSAGE
import com.farhanrr.makeda.util.DAILY_REMINDER_MINUTE
import com.farhanrr.makeda.util.DAILY_REMINDER_TITLE
import com.farhanrr.makeda.util.TEST_REMINDER_ID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

data class MakedaUiState(
    val isLoggedIn: Boolean = false,
    val isGuest: Boolean = false,
    val accounts: List<AuthAccount> = emptyList(),
    val authError: String? = null,
    val transactions: List<Transaction> = emptyList(),
    val reminders: List<Reminder> = emptyList(),
    val saldo: Long = 0L,
    val totalPemasukan: Long = 0L,
    val totalPengeluaran: Long = 0L,
    val profile: UserProfile = UserProfile(),
    val settings: AppSettings = AppSettings()
)

// ---------- penyimpanan sederhana (teks) ----------
private const val REC = ";"
private const val FLD = "|"
private const val GUEST_KEY = "__guest__"
private const val ACCOUNTS_KEY = "makeda_accounts_v2"
private const val SESSION_KEY = "makeda_session_v2"
private const val SETTINGS_KEY = "makeda_settings_v2"
private const val TX_PREFIX = "makeda_tx_"
private const val REM_PREFIX = "makeda_rem_"
private const val PROFILE_PREFIX = "makeda_profile_"

private val TX_ORDER = compareByDescending<Transaction> { it.date }.thenByDescending { it.id }

private fun esc(s: String): String =
    s.replace("%", "%25").replace("|", "%7C").replace(";", "%3B").replace(",", "%2C").replace("\n", "%0A")

private fun unesc(s: String): String =
    s.replace("%0A", "\n").replace("%2C", ",").replace("%3B", ";").replace("%7C", "|").replace("%25", "%")

private fun encodeTx(t: Transaction): String = listOf(
    t.id.toString(), t.date.toString(), esc(t.title), t.amount.toString(),
    t.type.name, t.category.name, esc(t.customLabel ?: ""),
    (t.hour?.toString() ?: ""), (t.minute?.toString() ?: ""),
    t.photoPaths.joinToString(",") { esc(it) }
).joinToString(FLD)

private fun decodeTx(rec: String): Transaction? {
    val p = rec.split(FLD)
    if (p.size != 9 && p.size != 10) return null
    return try {
        Transaction(
            id = p[0].toLong(),
            date = LocalDate.parse(p[1]),
            title = unesc(p[2]),
            amount = p[3].toLong(),
            type = TransactionType.valueOf(p[4]),
            category = runCatching { TransactionCategory.valueOf(p[5]) }.getOrDefault(TransactionCategory.LAINNYA),
            customLabel = unesc(p[6]).ifBlank { null },
            hour = p[7].toIntOrNull(),
            minute = p[8].toIntOrNull(),
            photoPaths = p.getOrNull(9).orEmpty().split(",").map { unesc(it) }.filter { it.isNotBlank() }
        )
    } catch (e: Exception) {
        null
    }
}

private fun encodeReminder(r: Reminder): String = listOf(
    r.id.toString(), r.date.toString(), r.hour.toString(), r.minute.toString(), esc(r.title), esc(r.note)
).joinToString(FLD)

private fun decodeReminder(rec: String): Reminder? {
    val p = rec.split(FLD)
    if (p.size != 6) return null
    return try {
        Reminder(p[0].toInt(), LocalDate.parse(p[1]), p[2].toInt(), p[3].toInt(), unesc(p[4]), unesc(p[5]))
    } catch (e: Exception) {
        null
    }
}

private fun encodeProfile(p: UserProfile): String = listOf(
    esc(p.name), esc(p.email), esc(p.currency), p.monthlyBudgetTarget.toString(), esc(p.photoPath ?: ""),
    esc(p.goalName), p.goalTarget.toString(),
    p.categoryBudgets.entries.joinToString(",") { "${it.key}:${it.value}" }
).joinToString(FLD)

private fun decodeProfile(raw: String?): UserProfile? {
    if (raw.isNullOrBlank()) return null
    val p = raw.split(FLD)
    if (p.size != 5 && p.size != 7 && p.size != 8) return null
    return UserProfile(
        name = unesc(p[0]),
        email = unesc(p[1]),
        currency = unesc(p[2]),
        monthlyBudgetTarget = p[3].toLongOrNull() ?: 2_000_000L,
        photoPath = unesc(p[4]).ifBlank { null },
        goalName = p.getOrNull(5)?.let { unesc(it) } ?: "",
        goalTarget = p.getOrNull(6)?.toLongOrNull() ?: 0L,
        categoryBudgets = p.getOrNull(7).orEmpty().split(",").mapNotNull { e ->
            val kv = e.split(":")
            val v = kv.getOrNull(1)?.toLongOrNull()
            if (kv.size == 2 && v != null && v > 0) kv[0] to v else null
        }.toMap()
    )
}

class MakedaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MakedaUiState())
    val uiState: StateFlow<MakedaUiState> = _uiState.asStateFlow()

    private var nextTxId = 1L
    private var nextReminderId = 1
    private var currentUserKey: String? = null

    init {
        val accounts = loadAccounts()
        _uiState.update { it.copy(accounts = accounts, settings = loadSettings()) }
        applyDailyReminder(_uiState.value.settings)

        // Auto-login: kalau terakhir kali belum keluar, langsung masuk lagi
        val session = PlatformStore.getString(SESSION_KEY)
        if (!session.isNullOrBlank()) {
            if (session == GUEST_KEY) {
                enterUser(GUEST_KEY, null)
            } else {
                accounts.firstOrNull { it.email.lowercase() == session }?.let { enterUser(session, it) }
            }
        }
    }

    // ---------------- LOAD / SAVE ----------------

    private fun loadAccounts(): List<AuthAccount> {
        val raw = PlatformStore.getString(ACCOUNTS_KEY) ?: return emptyList()
        return raw.split(REC).filter { it.isNotBlank() }.mapNotNull { rec ->
            val p = rec.split(FLD)
            if (p.size == 3) AuthAccount(email = unesc(p[0]), password = unesc(p[1]), name = unesc(p[2])) else null
        }
    }

    private fun persistAccounts(accounts: List<AuthAccount>) {
        PlatformStore.putString(
            ACCOUNTS_KEY,
            accounts.joinToString(REC) { "${esc(it.email)}$FLD${esc(it.password)}$FLD${esc(it.name)}" }
        )
    }

    private fun loadSettings(): AppSettings {
        val p = (PlatformStore.getString(SETTINGS_KEY) ?: return AppSettings()).split(FLD)
        if (p.size < 3) return AppSettings()
        return AppSettings(darkMode = p[0] == "1", notificationsEnabled = p[1] == "1", biometricLockEnabled = p[2] == "1", dailyReminderEnabled = p.getOrNull(3)?.let { it == "1" } ?: true)
    }

    private fun persistSettings(s: AppSettings) {
        PlatformStore.putString(
            SETTINGS_KEY,
            listOf(if (s.darkMode) "1" else "0", if (s.notificationsEnabled) "1" else "0", if (s.biometricLockEnabled) "1" else "0", if (s.dailyReminderEnabled) "1" else "0")
                .joinToString(FLD)
        )
    }

    private fun persistUserData() {
        val key = currentUserKey ?: return
        val s = _uiState.value
        PlatformStore.putString(TX_PREFIX + key, s.transactions.joinToString(REC) { encodeTx(it) })
        PlatformStore.putString(REM_PREFIX + key, s.reminders.joinToString(REC) { encodeReminder(it) })
        PlatformStore.putString(PROFILE_PREFIX + key, encodeProfile(s.profile))
    }

    private fun withTransactions(s: MakedaUiState, list: List<Transaction>): MakedaUiState {
        val income = list.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expense = list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        return s.copy(
            transactions = list.sortedWith(TX_ORDER),
            saldo = income - expense,
            totalPemasukan = income,
            totalPengeluaran = expense
        )
    }

    private fun enterUser(key: String, account: AuthAccount?) {
        currentUserKey = key
        PlatformStore.putString(SESSION_KEY, key)

        val txs = (PlatformStore.getString(TX_PREFIX + key) ?: "")
            .split(REC).filter { it.isNotBlank() }.mapNotNull { decodeTx(it) }
        val reminders = (PlatformStore.getString(REM_PREFIX + key) ?: "")
            .split(REC).filter { it.isNotBlank() }.mapNotNull { decodeReminder(it) }
        val defaultProfile = if (account != null) {
            UserProfile(name = account.name, email = account.email)
        } else {
            UserProfile(name = "Tamu", email = "Mode tamu")
        }
        val profile = decodeProfile(PlatformStore.getString(PROFILE_PREFIX + key)) ?: defaultProfile

        nextTxId = (txs.maxOfOrNull { it.id } ?: 0L) + 1L
        nextReminderId = (reminders.maxOfOrNull { it.id } ?: 0) + 1

        _uiState.update {
            withTransactions(
                it.copy(
                    isLoggedIn = true,
                    isGuest = key == GUEST_KEY,
                    authError = null,
                    profile = profile,
                    reminders = reminders
                ),
                txs
            )
        }
    }

    // ---------------- AUTH ----------------

    fun register(name: String, email: String, password: String) {
        val cleanEmail = email.trim()
        when {
            name.isBlank() -> { setAuthError("Nama wajib diisi"); return }
            !cleanEmail.contains("@") || !cleanEmail.contains(".") -> { setAuthError("Format email tidak valid"); return }
            password.length < 4 -> { setAuthError("Kata sandi minimal 4 karakter"); return }
            _uiState.value.accounts.any { it.email.equals(cleanEmail, ignoreCase = true) } -> {
                setAuthError("Email sudah terdaftar, silakan masuk")
                return
            }
        }
        val account = AuthAccount(cleanEmail, password, name.trim())
        val accounts = _uiState.value.accounts + account
        _uiState.update { it.copy(accounts = accounts) }
        persistAccounts(accounts)
        enterUser(cleanEmail.lowercase(), account)
        persistUserData()
    }

    fun login(email: String, password: String) {
        val account = _uiState.value.accounts.firstOrNull {
            it.email.equals(email.trim(), ignoreCase = true) && it.password == password
        }
        if (account == null) {
            setAuthError("Email atau kata sandi salah")
        } else {
            enterUser(account.email.lowercase(), account)
        }
    }

    fun loginAsGuest() {
        enterUser(GUEST_KEY, null)
    }

    fun logout() {
        persistUserData()
        PlatformStore.putString(SESSION_KEY, "")
        currentUserKey = null
        _uiState.update {
            it.copy(
                isLoggedIn = false,
                isGuest = false,
                authError = null,
                transactions = emptyList(),
                reminders = emptyList(),
                saldo = 0L,
                totalPemasukan = 0L,
                totalPengeluaran = 0L,
                profile = UserProfile()
            )
        }
    }

    private fun setAuthError(msg: String) {
        _uiState.update { it.copy(authError = msg) }
    }

    fun clearAuthError() {
        _uiState.update { it.copy(authError = null) }
    }

    // ---------------- TRANSAKSI ----------------

    fun addTransaction(
        date: LocalDate,
        title: String,
        amount: Long,
        type: TransactionType,
        category: TransactionCategory = TransactionCategory.LAINNYA,
        customLabel: String? = null,
        hour: Int? = null,
        minute: Int? = null,
        photoPaths: List<String> = emptyList()
    ) {
        val newTx = Transaction(nextTxId++, date, title, amount, type, category, customLabel, hour, minute, photoPaths)
        _uiState.update { withTransactions(it, it.transactions + newTx) }
        persistUserData()
        notifyBudgetIfNeeded(newTx)
    }

    // Notifikasi saat pengeluaran satu kategori mencapai 80% dan 100% dari batas
    private fun notifyBudgetIfNeeded(tx: Transaction) {
        if (tx.type != TransactionType.EXPENSE) return
        val s = _uiState.value
        if (!s.settings.notificationsEnabled) return
        val limit = s.profile.categoryBudgets[tx.category.name] ?: return
        if (limit <= 0L) return
        val after = s.transactions.filter {
            it.type == TransactionType.EXPENSE && it.category == tx.category &&
                it.date.year == tx.date.year && it.date.monthNumber == tx.date.monthNumber
        }.sumOf { it.amount }
        val before = after - tx.amount
        val label = tx.category.label
        val msg: String = when {
            before < limit && after >= limit ->
                "Budget $label bulan ini sudah habis (${formatRupiah(after)} dari ${formatRupiah(limit)})."
            before * 100 < limit * 80 && after * 100 >= limit * 80 ->
                "Budget $label sudah terpakai 80%. Sisa ${formatRupiah(limit - after)}."
            else -> return
        }
        PlatformReminderScheduler.schedule(
            880000 + tx.category.ordinal, "Peringatan Budget $label", msg,
            kotlinx.datetime.Clock.System.now().toEpochMilliseconds() + 1500L
        )
    }

    fun updateTransaction(
        id: Long,
        date: LocalDate,
        title: String,
        amount: Long,
        type: TransactionType,
        category: TransactionCategory,
        customLabel: String? = null,
        hour: Int? = null,
        minute: Int? = null,
        photoPaths: List<String> = emptyList()
    ) {
        _uiState.update { current ->
            val list = current.transactions.map { tx ->
                if (tx.id == id) tx.copy(
                    date = date, title = title, amount = amount, type = type,
                    category = category, customLabel = customLabel, hour = hour, minute = minute,
                    photoPaths = photoPaths
                ) else tx
            }
            withTransactions(current, list)
        }
        persistUserData()
    }

    fun removeTransaction(id: Long) {
        _uiState.update { withTransactions(it, it.transactions.filterNot { tx -> tx.id == id }) }
        persistUserData()
    }

    fun clearAllTransactions() {
        _uiState.update { withTransactions(it, emptyList()) }
        persistUserData()
    }

    fun transactionsForDate(date: LocalDate): List<Transaction> =
        _uiState.value.transactions.filter { it.date == date }

    fun transactionsForMonth(year: Int, month: Int): List<Transaction> =
        _uiState.value.transactions.filter { it.date.year == year && it.date.monthNumber == month }

    // ---------------- PENGINGAT HARIAN (jam 20.00) ----------------

    private fun applyDailyReminder(s: AppSettings) {
        PlatformStore.putString("makeda_daily_enabled", if (s.dailyReminderEnabled) "1" else "0")
        if (s.dailyReminderEnabled) {
            PlatformReminderScheduler.scheduleDaily(
                DAILY_REMINDER_ID, DAILY_REMINDER_TITLE, DAILY_REMINDER_MESSAGE,
                DAILY_REMINDER_HOUR, DAILY_REMINDER_MINUTE
            )
        } else {
            PlatformReminderScheduler.cancel(DAILY_REMINDER_ID)
        }
    }

    fun sendTestNotification() {
        PlatformReminderScheduler.schedule(
            TEST_REMINDER_ID,
            "MAKEDA",
            "Tes berhasil! Pengingat menabung akan muncul tiap hari jam 20.00.",
            kotlinx.datetime.Clock.System.now().toEpochMilliseconds() + 3000L
        )
    }

    fun restoreTransaction(tx: Transaction) {
        _uiState.update { withTransactions(it, it.transactions + tx) }
        persistUserData()
    }

    // ---------------- PENGINGAT ----------------

    fun addReminder(date: LocalDate, hour: Int, minute: Int, title: String, note: String) {
        val id = nextReminderId++
        val reminder = Reminder(id, date, hour, minute, title, note)
        _uiState.update { it.copy(reminders = it.reminders + reminder) }
        persistUserData()
        PlatformReminderScheduler.schedule(id, title, note, epochMillisOf(date, hour, minute))
    }

    fun addReminderRange(startDate: LocalDate, days: Int, hour: Int, minute: Int, title: String, note: String) {
        val span = days.coerceIn(1, 90)
        repeat(span) { offset ->
            val date = startDate.plus(offset, kotlinx.datetime.DateTimeUnit.DAY)
            val id = nextReminderId++
            val reminder = Reminder(id, date, hour, minute, title, note)
            _uiState.update { it.copy(reminders = it.reminders + reminder) }
            PlatformReminderScheduler.schedule(id, title, note, epochMillisOf(date, hour, minute))
        }
        persistUserData()
    }

    fun removeReminder(id: Int) {
        _uiState.update { it.copy(reminders = it.reminders.filterNot { r -> r.id == id }) }
        persistUserData()
        PlatformReminderScheduler.cancel(id)
    }

    fun remindersForDate(date: LocalDate): List<Reminder> =
        _uiState.value.reminders.filter { it.date == date }

    // ---------------- PROFIL & PENGATURAN ----------------

    fun updateProfile(profile: UserProfile) {
        _uiState.update { it.copy(profile = profile) }
        persistUserData()
    }

    fun updateSettings(settings: AppSettings) {
        _uiState.update { it.copy(settings = settings) }
        persistSettings(settings)
        applyDailyReminder(settings)
    }
}