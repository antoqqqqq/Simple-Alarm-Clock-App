package com.example.myapplication

import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.AlarmClock
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AlarmSetterApp()
            }
        }
    }
}

@Composable
fun AlarmSetterApp() {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    // Use strings for hour and minute to allow empty fields
    var hourText by remember { mutableStateOf(calendar.get(Calendar.HOUR_OF_DAY).toString()) }
    var minuteText by remember { mutableStateOf(calendar.get(Calendar.MINUTE).toString()) }
    var alarmMessage by remember { mutableStateOf("My Alarm") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Alarm Setter", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        // Manual time input with separate fields
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Hour input
            OutlinedTextField(
                value = hourText,
                onValueChange = { newValue ->
                    // Allow empty field or digits only, max 2 chars
                    if (newValue.isEmpty() || (newValue.all { it.isDigit() } && newValue.length <= 2)) {
                        hourText = newValue
                    }
                },
                label = { Text("Hour") },
                singleLine = true,
                modifier = Modifier.width(80.dp)
            )

            Text(text = ":", modifier = Modifier.padding(horizontal = 8.dp))

            // Minute input
            OutlinedTextField(
                value = minuteText,
                onValueChange = { newValue ->
                    // Allow empty field or digits only, max 2 chars
                    if (newValue.isEmpty() || (newValue.all { it.isDigit() } && newValue.length <= 2)) {
                        minuteText = newValue
                    }
                },
                label = { Text("Min") },
                singleLine = true,
                modifier = Modifier.width(80.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Time picker button
        Button(onClick = {
            TimePickerDialog(
                context,
                { _, selectedHour, selectedMinute ->
                    hourText = selectedHour.toString()
                    minuteText = selectedMinute.toString().padStart(2, '0')
                },
                hourText.toIntOrNull() ?: calendar.get(Calendar.HOUR_OF_DAY),
                minuteText.toIntOrNull() ?: calendar.get(Calendar.MINUTE),
                true // 24-hour format
            ).show()
        }) {
            Text("Use Time Picker")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Message input
        OutlinedTextField(
            value = alarmMessage,
            onValueChange = { alarmMessage = it },
            label = { Text("Alarm Message") },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Set alarm button
        Button(
            onClick = {
                val hour = hourText.toIntOrNull()
                val minute = minuteText.toIntOrNull()

                if (hour == null || minute == null) {
                    Toast.makeText(context, "Please enter valid hour and minute", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (hour < 0 || hour > 23) {
                    Toast.makeText(context, "Hour must be between 0-23", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (minute < 0 || minute > 59) {
                    Toast.makeText(context, "Minute must be between 0-59", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                try {
                    setAlarm(context, hour, minute, alarmMessage)
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            modifier = Modifier.height(48.dp).width(200.dp)
        ) {
            Text("Set Alarm")
        }
    }
}

fun setAlarm(context: Context, hour: Int, minute: Int, message: String) {
    try {
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            putExtra(AlarmClock.EXTRA_MESSAGE, message)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
        }

        context.startActivity(intent)
        Toast.makeText(
            context,
            "Setting alarm for ${String.format("%02d:%02d", hour, minute)}",
            Toast.LENGTH_SHORT
        ).show()
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No alarm app found on this device", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
    }
}