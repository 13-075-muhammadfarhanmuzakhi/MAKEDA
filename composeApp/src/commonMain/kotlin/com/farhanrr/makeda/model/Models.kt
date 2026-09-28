package com.farhanrr.makeda.model

import kotlinx.datetime.LocalDate

enum class TransactionType { INCOME, EXPENSE }

enum class TransactionCategory(val label: String) {
    GAJI("Gaji"),
    TABUNGAN("Tabungan"),
    MAKANAN("Makanan"),
    TRANSPORT("Transport"),
    TAGIHAN("Tagihan"),
    HIBURAN("Hiburan"),
    BELANJA("Belanja"),
    LAINNYA("Lainnya")
}

val EXPENSE_CATEGORIES = listOf(
    TransactionCategory.MAKANAN, TransactionCategory.TRANSPORT, TransactionCategory.TAGIHAN,
    TransactionCategory.HIBURAN, TransactionCategory.BELANJA, TransactionCategory.LAINNYA
)

data class Transaction(
    val id: Long,
    val date: LocalDate,
    val title: String,
    val amount: Long,
    val type: TransactionType,
    val category: TransactionCategory = TransactionCategory.LAINNYA,
    val customLabel: String? = null,
    val hour: Int? = null,
    val minute: Int? = null,
    val photoPaths: List<String> = emptyList()
) {
    val displayCategory: String get() = customLabel?.takeIf { it.isNotBlank() } ?: category.label
    val displayTime: String? get() = if (hour != null && minute != null) "%02d:%02d".format(hour, minute) else null
}

data class Reminder(
    val id: Int,
    val date: LocalDate,
    val hour: Int,
    val minute: Int,
    val title: String,
    val note: String
)

data class UserProfile(
    val name: String = "Pengguna MAKEDA",
    val email: String = "user@makeda.app",
    val currency: String = "IDR",
    val monthlyBudgetTarget: Long = 2_000_000L,
    val photoPath: String? = null,
    val goalName: String = "",
    val goalTarget: Long = 0L,
    val categoryBudgets: Map<String, Long> = emptyMap()
)

data class AppSettings(
    val darkMode: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val biometricLockEnabled: Boolean = false,
    val dailyReminderEnabled: Boolean = true
)

data class AuthAccount(
    val email: String,
    val password: String,
    val name: String
)