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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    val color: Color,
    val studentName: String? = null
)

/**
 * Student Schedule Screen — Premium Redesign
 * Modern week selector, depth-enhanced event cards
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentScheduleScreen() {
    val authRepository = remember { AuthRepository() }
    val studentRepository = remember { StudentRepository() }
    
    var classes by remember { mutableStateOf<List<Pair<String?, StudentClassInfo>>>(emptyList()) }
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
    val parentRepository = remember { com.tmix.education.data.repository.ParentRepository() }
    
    LaunchedEffect(Unit) {
        scope.launch {
            isLoading = true
            error = null
            val userId = authRepository.getCurrentUserId()
            if (userId != null) {
                if (authRepository.isParent()) {
                    val parentResult = parentRepository.getParent(userId)
                    parentResult.onSuccess { parent ->
                        val allClasses = mutableListOf<Pair<String?, StudentClassInfo>>()
                        parent.students?.forEach { student ->
                            student.classes?.filter { it.classInfo.status == "active" }?.forEach { classInfo ->
                                allClasses.add(Pair(student.name, classInfo))
                            }
                        }
                        classes = allClasses
                    }.onFailure { e ->
                        error = e.message ?: "Không thể tải lịch học"
                    }
                } else {
                    val result = studentRepository.getStudent(userId)
                    result.onSuccess { student ->
                        classes = student.classes?.filter { it.classInfo.status == "active" }?.map { Pair(null, it) } ?: emptyList()
                    }.onFailure { e ->
                        error = e.message ?: "Không thể tải lịch học"
                    }
                }
            } else {
                error = "Chưa đăng nhập"
            }
            isLoading = false
        }
    }
    
    fun parseDayOfWeek(dayValue: String): DayOfWeek? {
        return when (dayValue.trim()) {
            "0" -> DayOfWeek.SUNDAY
            "1" -> DayOfWeek.MONDAY
            "2" -> DayOfWeek.TUESDAY
            "3" -> DayOfWeek.WEDNESDAY
            "4" -> DayOfWeek.THURSDAY
            "5" -> DayOfWeek.FRIDAY
            "6" -> DayOfWeek.SATURDAY
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
    
    val eventColors = listOf(TMixNavy, TMixRed, Info, Warning, Success)
    
    val scheduleByDay = remember(classes, currentWeekStart) {
        val result = mutableMapOf<LocalDate, List<ScheduleEvent>>()
        weekDays.forEach { date ->
            val eventsForDay = mutableListOf<ScheduleEvent>()
            classes.forEachIndexed { index, (studentName, enrollment) ->
                val classInfo = enrollment.classInfo
                val schedule = classInfo.schedule
                if (schedule != null && schedule.daysOfWeek != null) {
                    val classDays = schedule.daysOfWeek.mapNotNull { parseDayOfWeek(it) }
                    if (date.dayOfWeek in classDays) {
                        eventsForDay.add(
                            ScheduleEvent(
                                id = classInfo.id + (studentName ?: ""),
                                className = classInfo.name,
                                startTime = schedule.timeSlots?.startTime ?: "",
                                endTime = schedule.timeSlots?.endTime ?: "",
                                room = classInfo.room ?: "Chưa có phòng",
                                teacher = classInfo.teacher?.name ?: "Chưa phân công",
                                color = eventColors[index % eventColors.size],
                                studentName = studentName
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
                title = {
                    Text("Lịch học", style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold)
                },
                actions = {
                    TextButton(onClick = {
                        selectedDate = LocalDate.now()
                        currentWeekStart = LocalDate.now().minusDays(LocalDate.now().dayOfWeek.value.toLong() - 1)
                    }) {
                        Text("Hôm nay", style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold, color = TMixRed)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background)
        ) {
            // Month navigation
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { currentWeekStart = currentWeekStart.minusWeeks(1) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Tuần trước",
                        tint = TextSecondary)
                }
                Text(
                    currentWeekStart.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("vi")))
                        .replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold, color = TextPrimary
                )
                IconButton(onClick = { currentWeekStart = currentWeekStart.plusWeeks(1) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Tuần sau",
                        tint = TextSecondary)
                }
            }

            // Week selector — modern pill-style
            Card(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                shape = TMixShapes.CardLarge,
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                LazyRow(
                    Modifier.fillMaxWidth().padding(10.dp),
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
                                .clip(TMixShapes.Card)
                                .clickable { selectedDate = date }
                                .then(
                                    if (isSelected)
                                        Modifier.background(Brush.verticalGradient(listOf(TMixNavy, TMixNavyLight)))
                                    else Modifier
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                when (date.dayOfWeek.value) {
                                    1 -> "T2"; 2 -> "T3"; 3 -> "T4"; 4 -> "T5"
                                    5 -> "T6"; 6 -> "T7"; 7 -> "CN"; else -> ""
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = when {
                                    isSelected -> Color.White.copy(0.8f)
                                    isWeekend -> TMixRed
                                    else -> TextTertiary
                                }
                            )
                            Spacer(Modifier.height(6.dp))

                            Box(
                                Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> TMixRed
                                            isToday -> TMixNavyLight.copy(alpha = 0.15f)
                                            else -> Color.Transparent
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${date.dayOfMonth}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isSelected -> Color.White
                                        isToday -> TMixNavy
                                        isWeekend -> TMixRed
                                        else -> TextPrimary
                                    }
                                )
                            }

                            Spacer(Modifier.height(4.dp))
                            if (hasEvents) {
                                Box(
                                    Modifier.size(6.dp)
                                        .background(if (isSelected) Color.White else TMixRed, CircleShape)
                                )
                            } else {
                                Spacer(Modifier.height(6.dp))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Content
            when {
                isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = TMixRed)
                    }
                }
                error != null -> {
                    Box(Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
                        Card(
                            shape = TMixShapes.CardLarge,
                            colors = CardDefaults.cardColors(containerColor = ErrorLight),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.ErrorOutline, null, Modifier.size(48.dp), tint = Error)
                                Spacer(Modifier.height(12.dp))
                                Text(error ?: "Có lỗi xảy ra",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary, textAlign = TextAlign.Center)
                                Spacer(Modifier.height(16.dp))
                                Button(
                                    onClick = { /* TODO: retry */ },
                                    colors = ButtonDefaults.buttonColors(containerColor = TMixRed),
                                    shape = TMixShapes.Button
                                ) {
                                    Text("Thử lại", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
                classes.isEmpty() -> {
                    Box(Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
                        Card(
                            shape = TMixShapes.CardLarge,
                            colors = CardDefaults.cardColors(containerColor = SurfaceVariant.copy(alpha = 0.5f)),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.EventBusy, null, Modifier.size(48.dp), tint = TextTertiary)
                                Spacer(Modifier.height(12.dp))
                                Text("Chưa đăng ký lớp nào",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium, color = TextSecondary)
                                Text("Liên hệ trung tâm để đăng ký lớp học",
                                    style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                            }
                        }
                    }
                }
                todayEvents.isEmpty() -> {
                    Box(Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
                        Card(
                            shape = TMixShapes.CardLarge,
                            colors = CardDefaults.cardColors(containerColor = SuccessTint),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🎉", style = MaterialTheme.typography.displayMedium)
                                Spacer(Modifier.height(8.dp))
                                Text("Không có lịch học",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium, color = TextSecondary)
                                Text(
                                    if (selectedDate.dayOfWeek.value >= 6) "Cuối tuần nghỉ ngơi!" else "Chọn ngày khác để xem lịch",
                                    style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            // Event count badge
                            Surface(
                                color = NavyTint,
                                shape = TMixShapes.Chip
                            ) {
                                Text(
                                    "${todayEvents.size} buổi học",
                                    Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = TMixNavy
                                )
                            }
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
    Card(
        shape = TMixShapes.CardLarge,
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.fillMaxWidth()) {
            // Gradient left accent bar
            Box(
                Modifier
                    .width(6.dp)
                    .height(if (event.studentName != null) 130.dp else 110.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(event.color, event.color.copy(alpha = 0.4f))
                        )
                    )
            )

            Column(Modifier.padding(16.dp).weight(1f)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        event.className,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold, color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    if (event.startTime.isNotEmpty()) {
                        Surface(
                            color = event.color.copy(0.1f),
                            shape = TMixShapes.Badge
                        ) {
                            Text(
                                "${event.startTime} - ${event.endTime}",
                                Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = event.color
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(22.dp).clip(CircleShape)
                                .background(event.color.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, null, Modifier.size(12.dp), tint = event.color)
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(event.teacher, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(22.dp).clip(CircleShape)
                                .background(event.color.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Room, null, Modifier.size(12.dp), tint = event.color)
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(event.room, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }

                // Child name badge for parent view
                if (event.studentName != null) {
                    Spacer(Modifier.height(10.dp))
                    Surface(color = event.color.copy(0.08f), shape = TMixShapes.Badge) {
                        Row(
                            Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Face, null, Modifier.size(14.dp), tint = event.color)
                            Spacer(Modifier.width(4.dp))
                            Text(event.studentName, style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium, color = event.color)
                        }
                    }
                }
            }
        }
    }
}
