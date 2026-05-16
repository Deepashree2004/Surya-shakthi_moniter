package com.example.surya_shakthi_moniter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*

// Custom Colors
val AppGreen = Color(0xFF4CAF50)
val AppOrange = Color(0xFFFF9800)
val AppBlue = Color(0xFF3F51B5)
val AppRed = Color(0xFFD32F2F)
val DarkCard = Color(0xFF1A1A1A)
val InputBg = Color(0xFFF9F9F9)

// Data Model
data class EnergyLog(
    val id: String = UUID.randomUUID().toString(),
    val date: String,
    val weather: String,
    val generation: Double,
    val consumption: Double
) {
    val isExport: Boolean get() = generation > consumption
    val savedAmount: Double get() = generation // In kWh
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(primary = Color.Black)) {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf("Dashboard", "Log", "Savings", "Guide", "Settings")
    val icons = listOf(Icons.Default.GridView, Icons.Default.ElectricBolt, Icons.Default.BarChart, Icons.AutoMirrored.Filled.MenuBook, Icons.Default.Settings)

    // Reactive State Hoisted
    val energyLogs = remember { 
        mutableStateListOf(
            EnergyLog(date = "2026-05-06", weather = "Sunny", generation = 9.8, consumption = 7.2),
            EnergyLog(date = "2026-05-05", weather = "Cloudy", generation = 5.5, consumption = 6.0),
            EnergyLog(date = "2026-05-04", weather = "Rainy", generation = 2.1, consumption = 8.5)
        )
    }
    var electricityRate by remember { mutableDoubleStateOf(8.0) }
    var panelCapacity by remember { mutableDoubleStateOf(2.0) }

    Scaffold(
        topBar = { AppHeader() },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item, fontSize = 10.sp) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedItem) {
                0 -> DashboardScreen(energyLogs)
                1 -> LogScreen(energyLogs, electricityRate)
                2 -> SavingsScreen(energyLogs, electricityRate)
                4 -> SettingsScreen(panelCapacity, electricityRate) { newCap, newRate ->
                    panelCapacity = newCap
                    electricityRate = newRate
                }
                else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("${items[selectedItem]} Screen") }
            }
        }
    }
}

@Composable
fun AppHeader() {
    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(Color.Black), Alignment.Center) {
                Icon(Icons.Default.WbSunny, null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("SURYA-SHAKTI", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Solar Energy Monitor", fontSize = 12.sp, color = Color.Gray)
            }
        }
        Icon(Icons.Outlined.Notifications, "Alerts", Modifier.size(24.dp))
    }
}

@Composable
fun DashboardScreen(logs: List<EnergyLog>) {
    val latestLog = logs.firstOrNull()
    val totalGen = logs.sumOf { it.generation }
    val totalCons = logs.sumOf { it.consumption }

    val independence = if (totalCons > 0) ((totalGen / totalCons) * 100).coerceAtMost(100.0) else 0.0

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Bottom) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WbSunny, null, Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Surya-Shakti", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
                Text("Solar Energy Monitor", fontSize = 14.sp, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Today", fontSize = 12.sp, color = Color.Gray)
                Text(latestLog?.date ?: "No Data", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(24.dp))
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp), shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Eco, null, tint = AppGreen)
                    Spacer(Modifier.width(8.dp))
                    Text("Green Energy Independence", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(24.dp))
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { (independence / 100).toFloat() },
                        modifier = Modifier.size(180.dp),
                        strokeWidth = 20.dp,
                        color = AppGreen,
                        trackColor = Color(0xFFEEEEEE)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${independence.toInt()}%", fontSize = 48.sp, fontWeight = FontWeight.Bold)
                        Text("Solar Powered", fontSize = 14.sp, color = Color.Gray)
                    }
                }
                Spacer(Modifier.height(24.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceAround) {
                    LogStat("%.2f kWh".format(latestLog?.generation ?: 0.0), "Generated")
                    LogStat("%.2f kWh".format(latestLog?.consumption ?: 0.0), "Consumed")
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SummaryCard(Modifier.weight(1f), "TOTAL GENERATED", "%.1f kWh".format(totalGen), Icons.Default.WbSunny, true)
            SummaryCard(Modifier.weight(1f), "GRID USED", "%.1f kWh".format((totalCons - totalGen).coerceAtLeast(0.0)), Icons.Default.Power, false)
        }
    }
}

@Composable
fun SummaryCard(modifier: Modifier, title: String, value: String, icon: ImageVector, dark: Boolean) {
    Card(modifier.height(120.dp), colors = CardDefaults.cardColors(containerColor = if (dark) DarkCard else Color.White), elevation = CardDefaults.cardElevation(2.dp), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = if (dark) AppOrange else AppBlue, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (dark) Color.White else Color.Gray)
            }
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = if (dark) Color.White else Color.Black)
        }
    }
}

@Composable
fun LogScreen(logs: SnapshotStateList<EnergyLog>, rate: Double) {
    var showForm by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ElectricBolt, null, tint = AppOrange, modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(8.dp))
                Text("Energy Log", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { showForm = !showForm },
                colors = ButtonDefaults.buttonColors(containerColor = if (showForm) AppRed else Color.Black),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(if (showForm) Icons.Default.Close else Icons.Default.Add, null)
                Text(if (showForm) "Cancel" else "Add Entry")
            }
        }
        Spacer(Modifier.height(16.dp))
        if (showForm) {
            NewEnergyEntryForm { newLog ->
                logs.add(0, newLog)
                showForm = false
            }
        } else {
            logs.forEach { log ->
                LogItem(log, rate)
            }
        }
    }
}

@Composable
fun LogItem(log: EnergyLog, rate: Double) {
    val savedValue = log.generation * rate
    Card(Modifier.fillMaxWidth().padding(vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(1.dp), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Column {
                    Text(log.date, fontWeight = FontWeight.Bold)
                    Text(log.weather, fontSize = 12.sp, color = Color.Gray)
                }
                Surface(color = if (log.isExport) Color(0xFFE8F5E9) else Color(0xFFFFEBEE), shape = RoundedCornerShape(8.dp)) {
                    val diff = log.generation - log.consumption
                    val tag = if (log.isExport) "+%.1f export".format(diff) else "%.1f import".format(diff)
                    Text(tag, Modifier.padding(8.dp, 4.dp), color = if (log.isExport) AppGreen else AppRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                LogStat("${log.generation}", "Gen kWh")
                LogStat("${log.consumption}", "Used kWh")
                LogStat("₹${savedValue.toInt()}", "Saved", AppGreen)
            }
        }
    }
}

@Composable
fun LogStat(value: String, label: String, color: Color = Color.Black) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun SavingsScreen(logs: List<EnergyLog>, rate: Double) {
    val totalSaved = logs.sumOf { it.generation * rate }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("💰 Savings Report", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("Based on rate: ₹$rate/kWh", color = Color.Gray)

        Spacer(Modifier.height(24.dp))
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = DarkCard), shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.padding(24.dp)) {
                Text("Total Estimated Savings", color = Color.Gray)
                Text("₹${totalSaved.toInt()}", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                Text("Accumulated from all logs", color = Color.Gray, fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(24.dp))
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp), shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Energy Balance", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Canvas(Modifier.fillMaxWidth().height(150.dp)) {
                    val maxVal = logs.maxOfOrNull { it.generation.coerceAtLeast(it.consumption) }?.coerceAtLeast(1.0) ?: 1.0
                    val points = logs.take(7).reversed()

                    if (points.size > 1) {
                        drawPath(Path().apply {
                            points.forEachIndexed { i, p ->
                                val x = size.width * i / (points.size - 1)
                                val y = size.height - (size.height * (p.generation / maxVal)).toFloat()
                                if (i == 0) moveTo(x, y) else lineTo(x, y)
                            }
                        }, color = AppOrange, style = Stroke(width = 4.dp.toPx()))

                        drawPath(Path().apply {
                            points.forEachIndexed { i, p ->
                                val x = size.width * i / (points.size - 1)
                                val y = size.height - (size.height * (p.consumption / maxVal)).toFloat()
                                if (i == 0) moveTo(x, y) else lineTo(x, y)
                            }
                        }, color = AppBlue, style = Stroke(width = 4.dp.toPx()))
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    LegendItem("Generation", AppOrange)
                    Spacer(Modifier.width(16.dp))
                    LegendItem("Consumption", AppBlue)
                }
            }
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun NewEnergyEntryForm(onAdd: (EnergyLog) -> Unit) {
    var date by remember { mutableStateOf("2026-05-11") }
    var weather by remember { mutableStateOf("Sunny") }
    var gen by remember { mutableStateOf("") }
    var cons by remember { mutableStateOf("") }

    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp), shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.padding(20.dp)) {
            Text("New Energy Entry", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(20.dp))

            InputFieldLabel("Date")
            OutlinedTextField(value = date, onValueChange = { date = it }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors())

            Spacer(Modifier.height(16.dp))
            InputFieldLabel("Weather Condition")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Sunny" to Icons.Default.WbSunny, "Cloudy" to Icons.Default.Cloud, "Rainy" to Icons.Default.Umbrella).forEach { (w, icon) ->
                    WeatherChip(w, icon, weather == w, Modifier.weight(1f).clickable { weather = w })
                }
            }

            Spacer(Modifier.height(16.dp))
            InputFieldLabel("Generation (kWh)")
            OutlinedTextField(value = gen, onValueChange = { gen = it }, placeholder = { Text("e.g. 8.5") }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors())

            Spacer(Modifier.height(16.dp))
            InputFieldLabel("Consumption (kWh)")
            OutlinedTextField(value = cons, onValueChange = { cons = it }, placeholder = { Text("e.g. 6.0") }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors())

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    val g = gen.toDoubleOrNull() ?: 0.0
                    val c = cons.toDoubleOrNull() ?: 0.0
                    onAdd(EnergyLog(date = date, weather = weather, generation = g, consumption = c))
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Save Entry", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SettingsScreen(currentCap: Double, currentRate: Double, onSave: (Double, Double) -> Unit) {
    var cap by remember { mutableStateOf(currentCap.toString()) }
    var rate by remember { mutableStateOf(currentRate.toString()) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Settings, null, tint = Color(0xFFB39DDB), modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(8.dp))
            Text("Settings", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }
        Text("Configure your solar system parameters", color = Color.Gray, fontSize = 14.sp)

        Spacer(Modifier.height(24.dp))
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp), shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.padding(20.dp)) {
                Text("Solar Panel Configuration", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(24.dp))
                InputFieldLabel("Installed Panel Capacity (kW)")
                OutlinedTextField(value = cap, onValueChange = { cap = it }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors())

                Spacer(Modifier.height(20.dp))
                InputFieldLabel("Electricity Rate (₹ per kWh)")
                OutlinedTextField(value = rate, onValueChange = { rate = it }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors())

                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = {
                        onSave(cap.toDoubleOrNull() ?: currentCap, rate.toDoubleOrNull() ?: currentRate)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Save Settings", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable fun InputFieldLabel(text: String, modifier: Modifier = Modifier) = Text(text, fontSize = 12.sp, color = Color.Gray, modifier = modifier.padding(bottom = 4.dp))
@Composable fun textFieldColors() = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = InputBg, focusedContainerColor = InputBg, unfocusedBorderColor = Color.Transparent, focusedBorderColor = Color.LightGray)

@Composable fun WeatherChip(label: String, icon: ImageVector, selected: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Color.Black else Color.White)
            .border(1.dp, if (selected) Color.Black else Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = if (selected) Color.White else Color.Black)
        Text(label, fontSize = 10.sp, color = if (selected) Color.White else Color.Black)
    }
}
