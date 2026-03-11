package com.tmix.education.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tmix.education.data.api.ApiConfig
import com.tmix.education.data.model.SubmitTestRequest
import com.tmix.education.data.model.Test
import com.tmix.education.data.model.TestAttempt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI States for Tests
 */
sealed class TestListState {
    object Loading : TestListState()
    data class Success(val tests: List<Test>) : TestListState()
    data class Error(val message: String) : TestListState()
}

sealed class TestDetailState {
    object Loading : TestDetailState()
    data class Success(val test: Test) : TestDetailState()
    data class Error(val message: String) : TestDetailState()
}

sealed class SubmitState {
    object Idle : SubmitState()
    object Submitting : SubmitState()
    data class Success(val attempt: TestAttempt) : SubmitState()
    data class Error(val message: String) : SubmitState()
}

/**
 * ViewModel for Student Test functionality
 */
class TestViewModel : ViewModel() {
    
    private val apiService = ApiConfig.getApiService()
    
    // Available tests list
    private val _testsState = MutableStateFlow<TestListState>(TestListState.Loading)
    val testsState: StateFlow<TestListState> = _testsState.asStateFlow()
    
    // Test detail (for taking)
    private val _testDetailState = MutableStateFlow<TestDetailState>(TestDetailState.Loading)
    val testDetailState: StateFlow<TestDetailState> = _testDetailState.asStateFlow()
    
    // Submit state
    private val _submitState = MutableStateFlow<SubmitState>(SubmitState.Idle)
    val submitState: StateFlow<SubmitState> = _submitState.asStateFlow()
    
    // Attempts history
    private val _attemptsState = MutableStateFlow<TestListState>(TestListState.Loading)
    val attemptsState: StateFlow<TestListState> = _attemptsState.asStateFlow()
    
    /**
     * Load available tests for student
     */
    fun loadAvailableTests() {
        viewModelScope.launch {
            _testsState.value = TestListState.Loading
            try {
                val response = apiService.getAvailableTests()
                if (response.isSuccessful) {
                    val tests = response.body()?.data ?: emptyList()
                    _testsState.value = TestListState.Success(tests)
                } else {
                    _testsState.value = TestListState.Error(
                        response.body()?.message ?: "Không thể tải danh sách đề thi"
                    )
                }
            } catch (e: Exception) {
                _testsState.value = TestListState.Error(
                    e.message ?: "Lỗi kết nối"
                )
            }
        }
    }
    
    /**
     * Load test detail for taking (without correct answers)
     */
    fun loadTestForTaking(testId: String) {
        viewModelScope.launch {
            _testDetailState.value = TestDetailState.Loading
            try {
                val response = apiService.getTestForStudent(testId)
                if (response.isSuccessful) {
                    val test = response.body()?.data
                    if (test != null) {
                        _testDetailState.value = TestDetailState.Success(test)
                    } else {
                        _testDetailState.value = TestDetailState.Error("Không tìm thấy đề thi")
                    }
                } else {
                    _testDetailState.value = TestDetailState.Error(
                        response.body()?.message ?: "Không thể tải đề thi"
                    )
                }
            } catch (e: Exception) {
                _testDetailState.value = TestDetailState.Error(
                    e.message ?: "Lỗi kết nối"
                )
            }
        }
    }
    
    /**
     * Submit test answers for grading
     */
    fun submitTest(testId: String, answers: List<Int>) {
        viewModelScope.launch {
            _submitState.value = SubmitState.Submitting
            try {
                val response = apiService.submitTest(testId, SubmitTestRequest(answers))
                if (response.isSuccessful) {
                    val attempt = response.body()?.data
                    if (attempt != null) {
                        _submitState.value = SubmitState.Success(attempt)
                    } else {
                        _submitState.value = SubmitState.Error("Nộp bài thất bại")
                    }
                } else {
                    _submitState.value = SubmitState.Error(
                        response.body()?.message ?: "Nộp bài thất bại"
                    )
                }
            } catch (e: Exception) {
                _submitState.value = SubmitState.Error(
                    e.message ?: "Lỗi kết nối"
                )
            }
        }
    }
    
    /**
     * Reset submit state (for new attempt)
     */
    fun resetSubmitState() {
        _submitState.value = SubmitState.Idle
    }
}
