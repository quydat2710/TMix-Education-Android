package com.tmix.education.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tmix.education.data.model.User
import com.tmix.education.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI State for Login Screen
 */
sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val user: User) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

/**
 * ViewModel for Login Screen
 */
class LoginViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {
    
    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()
    
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    
    init {
        checkLoginStatus()
    }
    
    /**
     * Check if user is already logged in
     */
    private fun checkLoginStatus() {
        _isLoggedIn.value = authRepository.isLoggedIn()
    }
    
    /**
     * Login with email and password
     */
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginState.value = LoginUiState.Error("Vui lòng nhập email và mật khẩu")
            return
        }
        
        viewModelScope.launch {
            _loginState.value = LoginUiState.Loading
            
            val result = authRepository.login(email, password)
            
            _loginState.value = result.fold(
                onSuccess = { user ->
                    _isLoggedIn.value = true
                    LoginUiState.Success(user)
                },
                onFailure = { error ->
                    LoginUiState.Error(error.message ?: "Đăng nhập thất bại")
                }
            )
        }
    }
    
    /**
     * Get the current logged in user
     */
    fun getCurrentUser(): User? = authRepository.getCurrentUser()
    
    /**
     * Check if current user is student
     */
    fun isStudent(): Boolean = authRepository.isStudent()
    
    /**
     * Check if current user is parent
     */
    fun isParent(): Boolean = authRepository.isParent()
    
    /**
     * Reset login state (for retry)
     */
    fun resetState() {
        _loginState.value = LoginUiState.Idle
    }
    
    /**
     * Logout
     */
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _isLoggedIn.value = false
            _loginState.value = LoginUiState.Idle
        }
    }
}
