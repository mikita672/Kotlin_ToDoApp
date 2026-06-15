package com.mdzeviatau.todoapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerDialogs(
    dueDateMillis: Long?,
    showDatePicker: Boolean,
    showTimePicker: Boolean,
    onDatePickerDismiss: () -> Unit,
    onTimePickerDismiss: () -> Unit,
    onDateSelected: (Long) -> Unit,
    onTimeSelected: (Long) -> Unit
) {
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dueDateMillis ?: System.currentTimeMillis()
        )
        DatePickerDialog(onDismissRequest = onDatePickerDismiss, confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { onDateSelected(it) }
            }) { Text("Ok") }
        }, dismissButton = {
            TextButton(onClick = onDatePickerDismiss) { Text("Cancel") }
        }) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val calendar = Calendar.getInstance().apply {
            dueDateMillis?.let { timeInMillis = it }
        }
        val timePickerState = rememberTimePickerState(
            initialHour = calendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = calendar.get(Calendar.MINUTE),
            is24Hour = true
        )

        AlertDialog(onDismissRequest = onTimePickerDismiss, confirmButton = {
            TextButton(onClick = {
                val date = Instant.ofEpochMilli(dueDateMillis ?: System.currentTimeMillis())
                    .atZone(ZoneId.systemDefault()).toLocalDate()
                val combinedDateTime = LocalDateTime.of(
                    date, LocalTime.of(timePickerState.hour, timePickerState.minute)
                )
                onTimeSelected(
                    combinedDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                )
            }) { Text("Ok") }
        }, dismissButton = {
            TextButton(onClick = onTimePickerDismiss) { Text("Cancel") }
        }, text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TimePicker(state = timePickerState)
            }
        })
    }
}
