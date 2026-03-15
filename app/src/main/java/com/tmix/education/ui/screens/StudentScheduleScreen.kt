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
import com.tmix.education.data.model.StudentClassInfo
import com.tmix.education.data.repository.AuthRepository
import com.tmix.education.data.repository.StudentRepository
import com.tmix.education.ui.theme.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
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
 * Weekly calendar view - loads enrolled classes and maps schedule.daysOfWeek to calendar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentScheduleScreen() {
    val authRepository = remember { AuthRepository() }
    val studentRepository = remember { StudentRepository() }
    
    var classes by remember { mutableStateOf<List<StudentClassInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentWeekStart by remember {
        mutableStateOf(LocalDate.now().minusDays(LocalDate.now().dayOfWeek.value.toLong() - 1))
    }

    val weekDays = remember(currentWeekStart) {
        (0..6).map { currentWeekStart.plusDays(it.toLong()) }
    }
    
    val scope = rememberCoroutineScope()
    
    // Load student's enrolled classes
    LaunchedEffect(Unit) {
        scope.launch {
            isLoading = true
            error = null
            val userId = authRepository.getCurrentUserId()
            if (userId != null) {
                val result = studentRepository.getStudent(userId)
                result.onSuccess { student ->
                    classes = student.classes ?: emptyList()
                }.onFailure { e ->
                    error = e.message ?: "Không thể tải lịch học"
                }
            } else {
                error = "Chưa đăng nhập"
            }
            isLoading = false
        }
    }
    
    // Map day values to DayOfWeek for matching
    // Backend stores days_of_week as numeric strings: "0"=Sunday, "1"=Monday, ..., "6"=Saturday
    fun parseDayOfWeek(dayValue: String): DayOfWeek? {
        return when (dayValue.trim()) {
            // Numeric format (backend standard)
            "0" -> DayOfWeek.SUNDAY
            "1" -> DayOfWeek.MONDAY
            "2" -> DayOfWeek.TUESDAY
            "3" -> DayOfWeek.WEDNESDAY
            "4" -> DayOfWeek.THURSDAY
            "5" -> DayOfWeek.FRIDAY
            "6" -> DayOfWeek.SATURDAY
            // Text fallback
            "monday", "thứ hai", "t2" -> DayOfWeek.MONDAY
            "tuesday", "thứ ba", "t3" -> DayOfWeek.TUESDAY
            "wednesday", "thứ tư", "t4" -> DayOfWeek.WEDNESDAY
            "thursday", "thứ năm", "t5" -> DayOfWeek.THURSDAY
            "friday", "thứ sáu", "t6" -> DayOfWeek.FRIDAY
            "saturday", "thứ bảy", "t7" -> DayOfWeek.SATURDAY
            "sunday", "chủ nhật", "cn" -> DayOfWeek.SUNDAY
            else -> null
        }
    }
    
    // Generate events for the selected date based on class schedules
    val eventColors = listOf(TMixNavy, TMixRed, Info, Warning, Success)
    
    val scheduleByDay = remember(classes, currentWeekStart) {
        val result = mutableMapOf<LocalDate, List<ScheduleEvent>>()
        
        weekDays.forEach { date ->
            val eventsForDay = mutableListOf<ScheduleEvent>()
            
            classes.forEachIndexed { index, enrollment ->
                val classInfo = enrollment.classInfo
                val schedule = classInfo.schedule
                
                if (schedule != null && schedule.daysOfWeek != null) {
                    val classDays = schedule.daysOfWeek.mapNotNull { parseDayOfWeek(it) }
                    
                    if (date.dayOfWeek in classDays) {
                        eventsForDay.add(
                            ScheduleEvent(
                                id = classInfo.id,
                                className = classInfo.name,
                                startTime = schedule.timeSlots?.startTime ?: "",
                                endTime = schedule.timeSlots?.endTime ?: "",
                                room = classInfo.room ?: "Chưa có phòng",
                                teacher = classInfo.teacher?.name ?: "Chưa phân công",
                                color = eventColors[index % eventColors.size]
                            )
                        )
                    }
                }
            }
            
            if (eventsForDay.isNotEmpty()) {
                result[date] = eventsForDay
            }
        }
        
        result
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

            // Content
            when {
                isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = TMixRed)
                    }
                }
                error != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ErrorOutline, null, Modifier.size(64.dp), tint = Error)
                            Spacer(Modifier.height(16.dp))
                            Text(error ?: "Có lỗi xảy ra", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { /* TODO: retry */ },
                                colors = ButtonDefaults.buttonColors(containerColor = TMixRed)
                            ) {
                                Text("Thử lại")
                            }
                        }
                    }
                }
                classes.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.EventBusy, null, Modifier.size(64.dp), tint = TextSecondary.copy(0.5f))
                            Spacer(Modifier.height(16.dp))
                            Text("Chưa đăng ký lớp nào", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                            Text("Liên hệ trung tâm để đăng ký lớp học", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                }
                todayEvents.isEmpty() -> {
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
                }
                else -> {
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
