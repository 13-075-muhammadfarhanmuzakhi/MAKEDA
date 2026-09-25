package com.farhanrr.makeda.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farhanrr.makeda.model.AppSettings
import com.farhanrr.makeda.model.AuthAccount
import com.farhanrr.makeda.model.Reminder
import com.farhanrr.makeda.model.Transaction
import com.farhanrr.makeda.model.TransactionCategory
import com.farhanrr.makeda.model.TransactionType
import com.farhanrr.makeda.model.UserProfile
import com.farhanrr.makeda.reminder.PlatformReminderScheduler
import com.farhanrr.makeda.util.epochMillisOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

data class MakedaUiState(
    val isLoggedIn: Boolean = false,
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

class MakedaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MakedaUiState())
    val uiState: StateFlow<MakedaUiState> = _uiState.asStateFlow()

    private var nextTxId = 1L
    private var nextReminderId = 1

    init {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        seedSample(today)
    }

    private fun seedSample(today: LocalDate) {
        addTransaction(LocalDate(today.year, today.month, 1), "Gajian", 3_000_000, TransactionType.INCOME, TransactionCategory.GAJI)
        addTransaction(today, "Makan Siang", 35_000, TransactionType.EXPENSE, TransactionCategory.MAKANAN)
        addTransaction(today, "Transport", 15_000, TransactionType.EXPENSE, TransactionCategory.TRANSPORT)
    }

    // ---------------- AUTH ----------------

    fun register(name: String, email: String, password: String) {
        val state = _uiState.value
        if (state.accounts.any { it.email.equals(email, ignoreCase = true) }) {
            _uiState.update { it.copy(authError = "Email sudah terdaftar") }
            return
        }
        val account = AuthAccount(email, password, name)
        _uiState.update {
            it.copy(
                accounts = it.accounts + account,
                isLoggedIn = true,
                authError = null,
                profile = it.profile.copy(name = name, email = email)
            )
        }
    }

    fun login(email: String, password: String) {
        val state = _uiState.value
        val account = state.accounts.firstOrNull {
            it.email.equals(email, ignoreCase = true) && it.password == password
        }
        if (account == null) {
            _uiState.update { it.copy(authError = "Email atau kata sandi salah") }
        } else {
            _uiState.update {
                it.copy(
                    isLoggedIn = true,
                    authError = null,
                    profile = it.profile.copy(name = account.name, email = account.email)
                )
            }
        }
    }

    fun logout() {
        _uiState.update { it.copy(isLoggedIn = false) }
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
        minute: Int? = null
    ) {
        viewModelScope.launch {
            val newTx = Transaction(nextTxId++, date, title, amount, type, category, customLabel, hour, minute)
            _uiState.update { current ->
                val list = current.transactions + newTx
                current.copy(
                    transactions = list.sortedByDescending { it.date },
                    saldo = computeSaldo(list),
                    totalPemasukan = list.filter { it.type == TransactionType.INCOME }.sumOf { it.amount },
                    totalPengeluaran = list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                )
            }
        }
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
        minute: Int? = null
    ) {
        _uiState.update { current ->
            val list = current.transactions.map { tx ->
                if (tx.id == id) tx.copy(date = date, title = title, amount = amount, type = type, category = category, customLabel = customLabel, hour = hour, minute = minute) else tx
            }
            current.copy(
                transactions = list.sortedByDescending { it.date },
                saldo = computeSaldo(list),
                totalPemasukan = list.filter { it.type == TransactionType.INCOME }.sumOf { it.amount },
                totalPengeluaran = list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            )
        }
    }

    fun removeTransaction(id: Long) {
        _uiState.update { current ->
            val list = current.transactions.filterNot { it.id == id }
            current.copy(
                transactions = list,
                saldo = computeSaldo(list),
                totalPemasukan = list.filter { it.type == TransactionType.INCOME }.sumOf { it.amount },
                totalPengeluaran = list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            )
        }
    }

    fun transactionsForDate(date: LocalDate): List<Transaction> =
        _uiState.value.transactions.filter { it.date == date }

    fun transactionsForMonth(year: Int, month: Int): List<Transaction> =
        _uiState.value.transactions.filter { it.date.year == year && it.date.monthNumber == month }

    private fun computeSaldo(list: List<Transaction>): Long {
        val income = list.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expense = list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        return income - expense
    }

    // ---------------- PENGINGAT ----------------

    fun addReminder(date: LocalDate, hour: Int, minute: Int, title: String, note: String) {
        val id = nextReminderId++
        val reminder = Reminder(id, date, hour, minute, title, note)
        _uiState.update { it.copy(reminders = it.reminders + reminder) }
        PlatformReminderScheduler.schedule(id, title, note, epochMillisOf(date, hour, minute))
    }

    fun removeReminder(id: Int) {
        _uiState.update { it.copy(reminders = it.reminders.filterNot { r -> r.id == id }) }
        PlatformReminderScheduler.cancel(id)
    }

    fun remindersForDate(date: LocalDate): List<Reminder> =
        _uiState.value.reminders.filter { it.date == date }

    // ---------------- PROFIL & PENGATURAN ----------------

    fun updateProfile(profile: UserProfile) {
        _uiState.update { it.copy(profile = profile) }
    }

    fun updateSettings(settings: AppSettings) {
        _uiState.update { it.copy(settings = settings) }
    }
}
