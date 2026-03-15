package com.tmix.education.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tmix.education.data.model.User
import com.tmix.education.data.repository.AuthRepository
import com.tmix.education.data.repository.StudentRepository
import com.tmix.education.data.repository.ParentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI State for Profile operations
 */
sealed class ProfileUiState {
    object Idle : ProfileUiState()
    object Loading : ProfileUiState()
    data class Success(val message: String) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

/**
 * ViewModel for Profile operations (Edit Profile & Change Password)
 * Connects to real backend API for both Student and Parent roles
 */
class ProfileViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val studentRepository: StudentRepository = StudentRepository(),
    private val parentRepository: ParentRepository = ParentRepository()
) : ViewModel() {
    
    private val _updateState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    val updateState: StateFlow<ProfileUiState> = _updateState.asStateFlow()
    
    private val _changePasswordState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    val changePasswordState: StateFlow<ProfileUiState> = _changePasswordState.asStateFlow()
    
    /**
     * Get current user info
     */
    fun getCurrentUser(): User? = authRepository.getCurrentUser()
    
    /**
     * Check if current user is a student
     */
    fun isStudent(): Boolean = authRepository.isStudent()
    
    /**
     * Update user profile - calls the appropriate API based on role
     */
    fun updateProfile(
        name: String,
        phone: String,
        dayOfBirth: String,
        address: String,
        gender: String? = null
    ) {
        val userId = authRepository.getCurrentUserId() ?: return
        
        viewModelScope.launch {
            _updateState.value = ProfileUiState.Loading
            
            val updates = mutableMapOf<String, Any>()
            if (name.isNotBlank()) updates["name"] = name
            if (phone.isNotBlank()) updates["phone"] = phone
            if (dayOfBirth.isNotBlank()) updates["dayOfBirth"] = dayOfBirth
            if (address.isNotBlank()) updates["address"] = address
            if (gender != null) updates["gender"] = gender
            
            val result = if (isStudent()) {
                studentRepository.updateProfile(userId, updates)
            } else {
                parentRepository.updateProfile(userId, updates)
            }
            
            _updateState.value = result.fold(
                onSuccess = { ProfileUiState.Success("Cập nhật thông tin thành công!") },
                onFailure = { ProfileUiState.Error(it.message ?: "Cập nhật thất bại") }
            )
        }
    }
    
    /**
     * Change password - calls PATCH /auth/change-password
     */
    fun changePassword(
        oldPassword: String,
        newPassword: String,
        confirmPassword: String
    ) {
        if (oldPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
            _changePasswordState.value = ProfileUiState.Error("Vui lòng điền đầy đủ thông tin")
            return
        }
        
        if (newPassword.length < 8) {
            _changePasswordState.value = ProfileUiState.Error("Mật khẩu mới phải có ít nhất 8 ký tự")
            return
        }
        
        if (newPassword != confirmPassword) {
            _changePasswordState.value = ProfileUiState.Error("Mật khẩu xác nhận không khớp")
            return
        }
        
        viewModelScope.launch {
            _changePasswordState.value = ProfileUiState.Loading
            
            val result = authRepository.changePassword(oldPassword, newPassword, confirmPassword)
            
            _changePasswordState.value = result.fold(
                onSuccess = { ProfileUiState.Success("Đổi mật khẩu thành công!") },
                onFailure = { ProfileUiState.Error(it.message ?: "Đổi mật khẩu thất bại") }
            )
        }
    }
    
    /**
     * Reset update state
     */
    fun resetUpdateState() {
        _updateState.value = ProfileUiState.Idle
    }
    
    /**
     * Reset change password state
     */
    fun resetChangePasswordState() {
        _changePasswordState.value = ProfileUiState.Idle
    }
}
