package com.farhanrr.makeda.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.unit.dp

/** Field teks jam:menit yang, saat diketuk, membuka jam analog (jarum bisa digeser). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalogTimeField(
    label: String,
    hour: Int,
    minute: Int,
    modifier: Modifier = Modifier,
    onTimeChange: (Int, Int) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = "%02d:%02d".format(hour, minute),
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        trailingIcon = {
            IconButton(onClick = { showPicker = true }) {
                Icon(Icons.Filled.AccessTime, contentDescription = "Pilih jam")
            }
        },
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    )
    if (showPicker) {
        AnalogTimePickerDialog(
            initialHour = hour,
            initialMinute = minute,
            onDismiss = { showPicker = false },
            onConfirm = { h, m -> onTimeChange(h, m); showPicker = false }
        )
    }
}

/** Dialog jam analog: geser jarum pendek untuk jam, jarum panjang untuk menit. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalogTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    val state = rememberTimePickerState(initialHour = initialHour, initialMinute = initialMinute, is24Hour = true)
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp)) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Geser jarum jam", style = androidx.compose.material3.MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(12.dp))
                TimePicker(state = state)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "= %02d.%02d".format(state.hour, state.minute),
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Batal") }
                    TextButton(onClick = { onConfirm(state.hour, state.minute) }) { Text("OK") }
                }
            }
        }
    }
}