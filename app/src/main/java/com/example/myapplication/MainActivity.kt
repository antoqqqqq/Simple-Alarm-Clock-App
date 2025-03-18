package com.example.myapplication

import android.app.DatePickerDialog
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
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AlarmSetterApp()
        }
    }
}

@Composable
fun AlarmSetterApp() {
    val calendar = Calendar.getInstance()

    var hourText by remember { mutableStateOf(calendar.get(Calendar.HOUR_OF_DAY).toString()) }
    var minuteText by remember { mutableStateOf(calendar.get(Calendar.MINUTE).toString()) }
    var selectedDate by remember { mutableStateOf("") }
    var selectedCalendar by remember { mutableStateOf(calendar.clone() as Calendar) }
    var repeatWeekly by remember { mutableStateOf(false) }

    val context = LocalContext.current

    fun updateSelectedDate(year: Int, month: Int, day: Int) {
        val newCalendar = Calendar.getInstance()
        newCalendar.set(year, month, day,
            hourText.toIntOrNull() ?: calendar.get(Calendar.HOUR_OF_DAY),
            minuteText.toIntOrNull() ?: calendar.get(Calendar.MINUTE))
        selectedCalendar = newCalendar.clone() as Calendar

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        selectedDate = "${dateFormat.format(newCalendar.time)} ${hourText}:${minuteText}"
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Select Alarm Time", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Hour:")
            Spacer(modifier = Modifier.width(8.dp))
            TextField(
                value = hourText,
                onValueChange = {
                    val filtered = it.filter { char -> char.isDigit() }.take(2)
                    val hour = filtered.toIntOrNull()?.coerceIn(0, 23)?.toString() ?: ""
                    if (filtered.isNotEmpty()) {
                        hourText = hour
                        if (selectedDate.isNotEmpty()) {
                            val parts = selectedDate.split(" ")[0] // Keep the date
                            selectedDate = "$parts $hour:$minuteText"

                            // Update the calendar too
                            selectedCalendar.set(Calendar.HOUR_OF_DAY, hour.toIntOrNull() ?: 0)
                        }
                    }
                },
                modifier = Modifier.width(60.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = "Minute:")
            Spacer(modifier = Modifier.width(8.dp))
            TextField(
                value = minuteText,
                onValueChange = {
                    val filtered = it.filter { char -> char.isDigit() }.take(2)
                    val minute = filtered.toIntOrNull()?.coerceIn(0, 59)?.toString() ?: ""
                    if (filtered.isNotEmpty()) {
                        minuteText = minute
                        if (selectedDate.isNotEmpty()) {
                            val parts = selectedDate.split(" ")[0] // Keep the date
                            selectedDate = "$parts $hourText:$minute"

                            // Update the calendar too
                            selectedCalendar.set(Calendar.MINUTE, minute.toIntOrNull() ?: 0)
                        }
                    }
                },
                modifier = Modifier.width(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            DatePickerDialog(context, { _, year, month, dayOfMonth ->
                updateSelectedDate(year, month, dayOfMonth)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }) {
            Text(text = "Select Date")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Selected Date: $selectedDate")

        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = repeatWeekly,
                onCheckedChange = { repeatWeekly = it }
            )
            Text(text = "Repeat Weekly")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            val hour = hourText.toIntOrNull()
            val minute = minuteText.toIntOrNull()

            if (hour == null || minute == null) {
                Toast.makeText(context, "Please enter valid time values", Toast.LENGTH_SHORT).show()
                return@Button
            }

            if (selectedDate.isEmpty()) {
                Toast.makeText(context, "Please select a date", Toast.LENGTH_SHORT).show()
                return@Button
            }

            setAlarm(context, hour, minute, selectedDate, selectedCalendar, repeatWeekly)
        }) {
            Text(text = "Set Alarm")
        }
    }
}

fun setAlarm(context: Context, hour: Int, minute: Int, date: String, calendar: Calendar, repeatWeekly: Boolean) {
    val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
        putExtra(AlarmClock.EXTRA_HOUR, hour)
        putExtra(AlarmClock.EXTRA_MINUTES, minute)
        putExtra(AlarmClock.EXTRA_MESSAGE, "Alarm set for $date")
        putExtra(AlarmClock.EXTRA_VIBRATE, true)
        putExtra(AlarmClock.EXTRA_SKIP_UI, false)

        // Only add days if repeatWeekly is true
        if (repeatWeekly) {
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            putExtra(AlarmClock.EXTRA_DAYS, arrayListOf(dayOfWeek))
        }
    }

    try {
        context.startActivity(intent)
        Toast.makeText(context, "Alarm set for $date", Toast.LENGTH_SHORT).show()
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No alarm app available", Toast.LENGTH_LONG).show()
    }
}