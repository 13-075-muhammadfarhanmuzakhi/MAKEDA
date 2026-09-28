package com.farhanrr.makeda.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

fun formatRupiah(amount: Long): String {
    val negative = amount < 0
    val s = (if (negative) -amount else amount).toString()
    val sb = StringBuilder()
    var count = 0
    for (i in s.length - 1 downTo 0) {
        sb.append(s[i])
        count++
        if (count % 3 == 0 && i != 0) sb.append('.')
    }
    return (if (negative) "-Rp " else "Rp ") + sb.reverse().toString()
}

fun epochMillisOf(date: LocalDate, hour: Int, minute: Int): Long {
    val dateTime = LocalDateTime(date.year, date.month, date.dayOfMonth, hour, minute)
    return dateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
}

fun digitsOnly(input: String): String = input.filter { it.isDigit() }.take(13)

/** Menampilkan angka dengan spasi tiap 3 digit (200000 -> 200 000), nilai asli tetap angka murni. */
class ThousandsTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val n = raw.length
        val out = StringBuilder()
        for (i in 0 until n) {
            out.append(raw[i])
            val rem = n - 1 - i
            if (rem > 0 && rem % 3 == 0) out.append(' ')
        }
        val shown = out.toString()
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                var spaces = 0
                for (i in 0 until offset.coerceIn(0, n)) {
                    val rem = n - 1 - i
                    if (rem > 0 && rem % 3 == 0) spaces++
                }
                return offset + spaces
            }

            override fun transformedToOriginal(offset: Int): Int {
                var spaces = 0
                for (i in 0 until offset.coerceIn(0, shown.length)) if (shown[i] == ' ') spaces++
                return (offset - spaces).coerceIn(0, n)
            }
        }
        return TransformedText(AnnotatedString(shown), mapping)
    }
}