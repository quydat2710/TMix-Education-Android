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
import okhttp3.MediaType.Companion.toMediaTypeOrNull

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
            if (dayOfBirth.isNotBlank()) {
                // Backend expects MM/dd/yyyy or ISO (yyyy-MM-dd)
                // App UI uses dd/MM/yyyy ("13/01/2009"). We must convert.
                try {
                    val parts = dayOfBirth.split("/")
                    if (parts.size == 3) {
                        val formattedDate = "${parts[2]}-${parts[1]}-${parts[0]}" // yyyy-MM-dd
                        updates["dayOfBirth"] = formattedDate // send as ISO string
                    } else {
                        updates["dayOfBirth"] = dayOfBirth
                    }
                } catch (e: Exception) {
                    updates["dayOfBirth"] = dayOfBirth
                }
            }
            if (address.isNotBlank()) updates["address"] = address
            if (gender != null) updates["gender"] = gender
            
            val result: Result<Any> = if (isStudent()) {
                studentRepository.updateProfile(userId, updates)
            } else {
                parentRepository.updateProfile(userId, updates)
            }
            
            if (result.isSuccess) {
                // Update local cache
                val currentUser = authRepository.getCurrentUser()
                if (currentUser != null) {
                    val updatedUser = currentUser.copy(
                        name = (updates["name"] as? String) ?: currentUser.name,
                        phone = (updates["phone"] as? String) ?: currentUser.phone,
                        // dayOfBirth might be yyyy-MM-dd now, let's just keep whatever backend stores
                        dayOfBirth = (updates["dayOfBirth"] as? String) ?: currentUser.dayOfBirth,
                        address = (updates["address"] as? String) ?: currentUser.address,
                        gender = (updates["gender"] as? String) ?: currentUser.gender
                    )
                    authRepository.updateCachedUser(updatedUser)
                }
                _updateState.value = ProfileUiState.Success("Cập nhật thông tin thành công!")
            } else {
                _updateState.value = ProfileUiState.Error(result.exceptionOrNull()?.message ?: "Cập nhật thất bại")
            }
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
    
    private val _avatarState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    val avatarState: StateFlow<ProfileUiState> = _avatarState.asStateFlow()
    
    /**
     * Upload avatar — Full 2-step Cloudinary flow:
     * 1. POST /files (multipart) → upload to Cloudinary → get {url, public_id}
     * 2. PATCH /user/avatar → save URL to user record
     */
    fun uploadAvatar(contentResolver: android.content.ContentResolver, imageUri: android.net.Uri) {
        viewModelScope.launch {
            _avatarState.value = ProfileUiState.Loading
            try {
                val api = com.tmix.education.data.api.ApiConfig.getApiService()
                
                // Step 1: Read image bytes from URI
                val inputStream = contentResolver.openInputStream(imageUri)
                    ?: throw Exception("Không thể đọc ảnh")
                val bytes = inputStream.readBytes()
                inputStream.close()
                
                // Get file name and mime type
                val mimeType = contentResolver.getType(imageUri) ?: "image/jpeg"
                val extension = when {
                    mimeType.contains("png") -> "png"
                    mimeType.contains("webp") -> "webp"
                    else -> "jpg"
                }
                val fileName = "avatar_${System.currentTimeMillis()}.$extension"
                
                // Create multipart body
                val requestBody = okhttp3.RequestBody.create(
                    mimeType.toMediaTypeOrNull(), bytes
                )
                val filePart = okhttp3.MultipartBody.Part.createFormData("file", fileName, requestBody)
                val pathBody = okhttp3.RequestBody.create(
                    "text/plain".toMediaTypeOrNull(), "avatars"
                )
                
                // Step 2: Upload file to Cloudinary via backend POST /files
                val uploadResponse = api.uploadFile(filePart, pathBody)
                
                if (!uploadResponse.isSuccessful) {
                    throw Exception("Lỗi upload server: ${uploadResponse.code()}")
                }
                
                // Backend wraps response in { statusCode, message, data: { url, public_id } }
                val apiResponse = uploadResponse.body() ?: throw Exception("Response trống")
                val dataMap = apiResponse.data ?: throw Exception("Không có dữ liệu ảnh trả về")
                
                val imageUrl = dataMap["url"]?.toString() ?: throw Exception("Không có URL ảnh")
                val publicId = dataMap["public_id"]?.toString() ?: ""
                
                // Step 3: Save avatar URL to user via PATCH /user/avatar
                val avatarRequest = mapOf("imageUrl" to imageUrl, "publicId" to publicId)
                val avatarResponse = api.uploadAvatar(avatarRequest)
                
                if (avatarResponse.isSuccessful) {
                    // Update local cache
                    val currentUser = authRepository.getCurrentUser()
                    if (currentUser != null) {
                        val updatedUser = currentUser.copy(avatar = imageUrl, publicId = publicId)
                        authRepository.updateCachedUser(updatedUser)
                    }
                    _avatarState.value = ProfileUiState.Success("Cập nhật ảnh đại diện thành công!")
                } else {
                    _avatarState.value = ProfileUiState.Error("Lưu avatar thất bại: ${avatarResponse.code()}")
                }
            } catch (e: Exception) {
                _avatarState.value = ProfileUiState.Error(e.message ?: "Lỗi upload ảnh")
            }
        }
    }
    
    /**
     * Reset update state
     */
    fun resetUpdateState() {
        _updateState.value = ProfileUiState.Idle
    }
    
    fun resetAvatarState() {
        _avatarState.value = ProfileUiState.Idle
    }
    
    /**
     * Reset change password state
     */
    fun resetChangePasswordState() {
        _changePasswordState.value = ProfileUiState.Idle
    }
}
