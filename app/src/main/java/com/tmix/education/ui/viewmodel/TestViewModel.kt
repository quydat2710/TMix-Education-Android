package com.tmix.education.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tmix.education.data.api.ApiConfig
import com.tmix.education.data.model.SubmitTestRequest
import com.tmix.education.data.model.SubmitWritingRequest
import com.tmix.education.data.model.Test
import com.tmix.education.data.model.TestAttempt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

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

sealed class AttemptHistoryState {
    object Loading : AttemptHistoryState()
    data class Success(val attempts: List<TestAttempt>) : AttemptHistoryState()
    data class Error(val message: String) : AttemptHistoryState()
}

sealed class AttemptDetailState {
    object Loading : AttemptDetailState()
    data class Success(val attempt: TestAttempt) : AttemptDetailState()
    data class Error(val message: String) : AttemptDetailState()
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
    
    // Attempt history for chart
    private val _attemptHistoryState = MutableStateFlow<AttemptHistoryState>(AttemptHistoryState.Loading)
    val attemptHistoryState: StateFlow<AttemptHistoryState> = _attemptHistoryState.asStateFlow()
    
    // Attempt detail
    private val _attemptDetailState = MutableStateFlow<AttemptDetailState>(AttemptDetailState.Loading)
    val attemptDetailState: StateFlow<AttemptDetailState> = _attemptDetailState.asStateFlow()
    
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
     * Load attempt history for chart display
     */
    fun loadAttemptHistory() {
        viewModelScope.launch {
            _attemptHistoryState.value = AttemptHistoryState.Loading
            try {
                val response = apiService.getMyAttempts(page = 1, limit = 50)
                if (response.isSuccessful) {
                    val attempts = response.body()?.data?.data ?: emptyList()
                    _attemptHistoryState.value = AttemptHistoryState.Success(attempts)
                } else {
                    _attemptHistoryState.value = AttemptHistoryState.Error(
                        response.body()?.message ?: "Không thể tải lịch sử bài thi"
                    )
                }
            } catch (e: Exception) {
                _attemptHistoryState.value = AttemptHistoryState.Error(
                    e.message ?: "Lỗi kết nối"
                )
            }
        }
    }
    
    /**
     * Load attempt detail by ID
     */
    fun loadAttemptDetail(attemptId: String) {
        viewModelScope.launch {
            _attemptDetailState.value = AttemptDetailState.Loading
            try {
                val response = apiService.getAttemptResult(attemptId)
                if (response.isSuccessful) {
                    val attempt = response.body()?.data
                    if (attempt != null) {
                        _attemptDetailState.value = AttemptDetailState.Success(attempt)
                    } else {
                        _attemptDetailState.value = AttemptDetailState.Error("Không tìm thấy kết quả")
                    }
                } else {
                    _attemptDetailState.value = AttemptDetailState.Error(
                        response.body()?.message ?: "Không thể tải chi tiết bài thi"
                    )
                }
            } catch (e: Exception) {
                _attemptDetailState.value = AttemptDetailState.Error(
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
    
    /**
     * Reset test detail state
     */
    fun resetTestDetail() {
        _testDetailState.value = TestDetailState.Loading
    }
    
    /**
     * Submit writing test
     */
    fun submitWriting(testId: String, text: String) {
        viewModelScope.launch {
            _submitState.value = SubmitState.Submitting
            try {
                val response = apiService.submitWriting(testId, SubmitWritingRequest(text))
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
     * Submit speaking test (audio file)
     */
    fun submitSpeaking(testId: String, audioFile: File) {
        viewModelScope.launch {
            _submitState.value = SubmitState.Submitting
            try {
                val requestFile = audioFile.asRequestBody("audio/*".toMediaTypeOrNull())
                val audioPart = MultipartBody.Part.createFormData("audio", audioFile.name, requestFile)
                val response = apiService.submitSpeaking(testId, audioPart)
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
}
