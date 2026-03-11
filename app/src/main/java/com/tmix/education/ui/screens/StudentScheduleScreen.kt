package com.tmix.education.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tmix.education.data.model.ScheduleItem
import com.tmix.education.ui.theme.*
import com.tmix.education.ui.viewmodel.StudentDashboardViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class ScheduleEvent(
    val id: String,
    val className: String,
    val startTime: String,
    val endTime: String,
    val room: String,
    val teacher: String,
    val color: Color
)

/**
 * Student Schedule Screen
 * Weekly calendar view with class events - connected to real backend data
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentScheduleScreen(
    viewModel: StudentDashboardViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentWeekStart by remember {
        mutableStateOf(LocalDate.now().minusDays(LocalDate.now().dayOfWeek.value.toLong() - 1))
    }

    val weekDays = remember(currentWeekStart) {
        (0..6).map { currentWeekStart.plusDays(it.toLong()) }
    }

    // Map real schedule data to ScheduleEvents, grouped by date
    val eventColors = listOf(TMixNavy, TMixRed, Info, Warning, Success)
    val scheduleByDay = remember(state.schedule) {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        state.schedule
            .filter { it.date != null }
            .groupBy { item ->
                try {
                    LocalDate.parse(item.date!!.take(10), dateFormatter)
                } catch (e: Exception) {
                    LocalDate.now()
                }
            }
            .mapValues { (_, items) ->
                items.mapIndexed { index, item ->
                    ScheduleEvent(
                        id = item.id ?: "${index}",
                        className = item.className ?: "Lớp học",
                        startTime = item.startTime ?: "",
                        endTime = item.endTime ?: "",
                        room = item.room ?: "Chưa có phòng",
                        teacher = item.teacherName ?: "Chưa phân công",
                        color = eventColors[index % eventColors.size]
                    )
                }
            }
    }

    val todayEvents = scheduleByDay[selectedDate] ?: emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lịch học", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = {
                        selectedDate = LocalDate.now()
                        currentWeekStart = LocalDate.now().minusDays(LocalDate.now().dayOfWeek.value.toLong() - 1)
                    }) {
                        Icon(Icons.Default.Today, "Hôm nay")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Month header
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { currentWeekStart = currentWeekStart.minusWeeks(1) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Tuần trước")
                }

                Text(
                    currentWeekStart.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("vi"))).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                IconButton(onClick = { currentWeekStart = currentWeekStart.plusWeeks(1) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Tuần sau")
                }
            }

            // Week selector
            Card(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = TMixShapes.Card
            ) {
                LazyRow(
                    Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    state = rememberLazyListState()
                ) {
                    items(weekDays) { date ->
                        val isSelected = date == selectedDate
                        val isToday = date == LocalDate.now()
                        val hasEvents = scheduleByDay.containsKey(date)
                        val isWeekend = date.dayOfWeek.value >= 6

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(TMixShapes.Chip)
                                .clickable { selectedDate = date }
                                .background(if (isSelected) TMixNavy else Color.Transparent)
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                when (date.dayOfWeek.value) {
                                    1 -> "T2"; 2 -> "T3"; 3 -> "T4"; 4 -> "T5"
                                    5 -> "T6"; 6 -> "T7"; 7 -> "CN"; else -> ""
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isWeekend) FontWeight.Bold else FontWeight.Normal,
                                color = when {
                                    isSelected -> Color.White.copy(0.8f)
                                    isWeekend -> TMixRed
                                    else -> TextSecondary
                                }
                            )
                            Spacer(Modifier.height(4.dp))

                            Surface(
                                onClick = { selectedDate = date },
                                shape = CircleShape,
                                color = when {
                                    isSelected -> TMixRed
                                    isToday -> TMixNavyLight
                                    else -> Color.Transparent
                                }
                            ) {
                                Text(
                                    "${date.dayOfMonth}",
                                    Modifier.padding(10.dp),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isSelected || isToday -> Color.White
                                        isWeekend -> TMixRed
                                        else -> TextPrimary
                                    }
                                )
                            }

                            if (hasEvents) {
                                Spacer(Modifier.height(4.dp))
                                Box(
                                    Modifier
                                        .size(6.dp)
                                        .background(if (isSelected) Color.White else TMixRed, CircleShape)
                                )
                            } else {
                                Spacer(Modifier.height(10.dp))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Loading state
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TMixRed)
                }
            } else if (todayEvents.isEmpty()) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎉", style = MaterialTheme.typography.displayMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Không có lịch học",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextSecondary
                        )
                        Text(
                            if (selectedDate.dayOfWeek.value >= 6) "Cuối tuần nghỉ ngơi!" else "Chọn ngày khác để xem lịch",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            "${todayEvents.size} buổi học",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                    }

                    items(todayEvents) { event ->
                        ScheduleEventCard(event)
                    }

                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
fun ScheduleEventCard(event: ScheduleEvent) {
    Card(shape = TMixShapes.Card, elevation = CardDefaults.cardElevation(2.dp)) {
        Row(Modifier.fillMaxWidth()) {
            Box(
                Modifier
                    .width(4.dp)
                    .height(100.dp)
                    .background(event.color)
            )

            Column(Modifier.padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        event.className,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (event.startTime.isNotEmpty()) {
                        Surface(color = event.color.copy(0.1f), shape = TMixShapes.Chip) {
                            Text(
                                "${event.startTime} - ${event.endTime}",
                                Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = event.color
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, Modifier.size(14.dp), tint = TextSecondary)
                        Spacer(Modifier.width(4.dp))
                        Text(event.teacher, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Room, null, Modifier.size(14.dp), tint = TextSecondary)
                        Spacer(Modifier.width(4.dp))
                        Text(event.room, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
        }
    }
}
