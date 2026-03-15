package com.tmix.education.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tmix.education.data.api.ApiConfig
import com.tmix.education.data.model.*
import com.tmix.education.data.repository.AuthRepository
import com.tmix.education.data.repository.StudentRepository
import com.tmix.education.data.repository.ClassRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI State for Student Dashboard
 */
data class StudentDashboardState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val student: Student? = null,
    val classes: List<StudentClassInfo> = emptyList(),
    val classProgress: Map<String, Float> = emptyMap(), // classId -> progress (0.0 - 1.0)
    val schedule: List<ScheduleItem> = emptyList(),
    val attendanceStats: AttendanceStats? = null,
    val upcomingTests: Int = 0,
    val error: String? = null
)

/**
 * ViewModel for Student Dashboard Screen
 */
class StudentDashboardViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val studentRepository: StudentRepository = StudentRepository(),
    private val classRepository: ClassRepository = ClassRepository()
) : ViewModel() {
    
    private val _state = MutableStateFlow(StudentDashboardState())
    val state: StateFlow<StudentDashboardState> = _state.asStateFlow()
    
    init {
        loadDashboard()
    }
    
    /**
     * Load all dashboard data
     */
    fun loadDashboard() {
        val user = authRepository.getCurrentUser() ?: return
        val userId = authRepository.getCurrentUserId() ?: return
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, user = user, error = null)
            
            // Load student profile
            val studentResult = studentRepository.getStudent(userId)
            studentResult.onSuccess { student ->
                _state.value = _state.value.copy(
                    student = student,
                    classes = student.classes ?: emptyList()
                )
                
                // Calculate progress for each class from sessions
                val progressMap = mutableMapOf<String, Float>()
                student.classes?.forEach { enrollment ->
                    enrollment.classInfo?.id?.let { classId ->
                        try {
                            val sessionsResult = classRepository.getClassSessions(classId, limit = 100)
                            sessionsResult.onSuccess { sessions ->
                                if (sessions.isNotEmpty()) {
                                    val completed = sessions.count { !it.isActive }
                                    progressMap[classId] = completed.toFloat() / sessions.size
                                } else {
                                    progressMap[classId] = 0f
                                }
                            }
                        } catch (_: Exception) {
                            progressMap[classId] = 0f
                        }
                    }
                }
                _state.value = _state.value.copy(classProgress = progressMap)
            }.onFailure { error ->
                _state.value = _state.value.copy(error = error.message)
            }
            
            // Load attendance stats
            val statsResult = studentRepository.getAttendanceStats(userId)
            statsResult.onSuccess { stats ->
                _state.value = _state.value.copy(attendanceStats = stats)
            }
            
            // Load upcoming tests count
            try {
                val apiService = ApiConfig.getApiService()
                val testsResponse = apiService.getAvailableTests()
                if (testsResponse.isSuccessful && testsResponse.body()?.data != null) {
                    val tests = testsResponse.body()?.data ?: emptyList()
                    // Count tests not yet attempted
                    val upcoming = tests.count { !it.hasAttempted }
                    _state.value = _state.value.copy(upcomingTests = upcoming)
                }
            } catch (_: Exception) {}
            
            _state.value = _state.value.copy(isLoading = false)
        }
    }
    
    /**
     * Refresh dashboard data
     */
    fun refresh() {
        loadDashboard()
    }
    
    /**
     * Get class details
     */
    suspend fun getClassDetails(classId: String): Result<ClassInfo> {
        return classRepository.getClass(classId)
    }
    
    /**
     * Get payments for student
     */
    suspend fun getPayments(): Result<List<Payment>> {
        val userId = authRepository.getCurrentUserId() ?: return Result.failure(Exception("Not logged in"))
        return studentRepository.getPayments(userId)
    }
    
    /**
     * Logout
     */
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}

