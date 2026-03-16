package com.tmix.education.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tmix.education.data.model.*
import com.tmix.education.data.repository.AuthRepository
import com.tmix.education.data.repository.ParentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI State for Parent Dashboard
 */
data class ParentDashboardState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val parent: Parent? = null,
    val children: List<Student> = emptyList(),
    val selectedChild: Student? = null,
    val selectedChildSchedule: List<ScheduleItem> = emptyList(),
    val selectedChildAttendance: AttendanceStats? = null,
    val payments: List<Payment> = emptyList(),
    val pendingPaymentsCount: Int = 0,
    val totalPendingAmount: Double = 0.0,
    val error: String? = null
)

/**
 * ViewModel for Parent Dashboard Screen
 */
class ParentDashboardViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val parentRepository: ParentRepository = ParentRepository()
) : ViewModel() {
    
    private val _state = MutableStateFlow(ParentDashboardState())
    val state: StateFlow<ParentDashboardState> = _state.asStateFlow()
    
    init {
        loadDashboard()
    }
    
    /**
     * Load all dashboard data
     */
    fun loadDashboard() {
        val user = authRepository.getCurrentUser() ?: return
        val parentId = authRepository.getCurrentUserId() ?: return
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, user = user, error = null)
            
            // Load parent profile with children
            val parentResult = parentRepository.getParent(parentId)
            parentResult.onSuccess { parent ->
                val children = parent.students ?: emptyList()
                _state.value = _state.value.copy(
                    parent = parent,
                    children = children
                )
                
                // Select first child by default
                if (children.isNotEmpty()) {
                    selectChild(children.first())
                }
            }.onFailure { error ->
                _state.value = _state.value.copy(error = error.message)
            }
            
            // Load all payments
            val paymentsResult = parentRepository.getAllChildrenPayments(parentId)
            paymentsResult.onSuccess { payments ->
                val pending = payments.filter { !it.isPaid }
                val pendingAmount = pending.sumOf { it.remainingAmount }
                
                if (pendingAmount > 0) {
                    _state.value = _state.value.copy(
                        payments = payments,
                        pendingPaymentsCount = pending.size,
                        totalPendingAmount = pendingAmount
                    )
                } else {
                    // Calculate estimated monthly fee from children's active classes
                    val children = _state.value.children
                    var estimatedMonthlyFee = 0.0
                    children.forEach { child ->
                        child.classes?.forEach { enrollment ->
                            val classInfo = enrollment.classInfo
                            if (classInfo != null && classInfo.status == "active") {
                                val feePerLesson = classInfo.feePerLesson ?: 0.0
                                val lessonsPerWeek = classInfo.schedule?.daysOfWeek?.size ?: 0
                                val monthlyFee = feePerLesson * lessonsPerWeek * 4 // ~4 weeks/month
                                val discount = enrollment.discountPercent / 100.0
                                estimatedMonthlyFee += monthlyFee * (1 - discount)
                            }
                        }
                    }
                    _state.value = _state.value.copy(
                        payments = payments,
                        pendingPaymentsCount = 0,
                        totalPendingAmount = estimatedMonthlyFee
                    )
                }
            }
            
            _state.value = _state.value.copy(isLoading = false)
        }
    }
    
    /**
     * Select a child to view details
     */
    fun selectChild(child: Student) {
        viewModelScope.launch {
            _state.value = _state.value.copy(selectedChild = child)
            
            // Load child's schedule
            val scheduleResult = parentRepository.getChildSchedule(child.id)
            scheduleResult.onSuccess { schedule ->
                _state.value = _state.value.copy(selectedChildSchedule = schedule)
            }
            
            // Load child's attendance stats
            val statsResult = parentRepository.getChildAttendanceStats(child.id)
            statsResult.onSuccess { stats ->
                _state.value = _state.value.copy(selectedChildAttendance = stats)
            }
        }
    }
    
    /**
     * Refresh dashboard data
     */
    fun refresh() {
        loadDashboard()
    }
    
    /**
     * Get child payments
     */
    suspend fun getChildPayments(childId: String): Result<List<Payment>> {
        return parentRepository.getChildPayments(childId)
    }
    
    /**
     * Get payment QR code
     */
    suspend fun getPaymentQRCode(paymentId: String, amount: Double): Result<QRCodeResponse> {
        return parentRepository.getPaymentQRCode(paymentId, amount)
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
