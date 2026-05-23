package com.tmix.education.data.api

import com.tmix.education.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * TMix Education API Service
 * Retrofit interface for all Backend endpoints
 */
interface ApiService {
    
    // =====================================================
    // AUTHENTICATION
    // =====================================================
    
    /**
     * User login (Student/Parent)
     * POST /auth/login (unified endpoint)
     */
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<ApiResponse<LoginResponse>>
    
    /**
     * Refresh access token
     * GET /auth/refresh
     */
    @GET("auth/refresh")
    suspend fun refreshToken(): Response<ApiResponse<TokenResponse>>
    
    /**
     * Change password
     * PATCH /auth/change-password
     */
    @PATCH("auth/change-password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): Response<ApiResponse<Unit>>
    
    // =====================================================
    // STUDENTS
    // =====================================================
    
    /**
     * Get student by ID
     * GET /students/:id
     */
    @GET("students/{id}")
    suspend fun getStudent(
        @Path("id") studentId: String
    ): Response<ApiResponse<Student>>
    
    /**
     * Get student schedule
     * GET /students/schedule/:id
     */
    @GET("students/schedule/{id}")
    suspend fun getStudentSchedule(
        @Path("id") studentId: String
    ): Response<ApiResponse<List<ScheduleItem>>>
    
    /**
     * Get student statistics
     * GET /students/statistics
     */
    @GET("students/statistics")
    suspend fun getStudentStatistics(): Response<ApiResponse<Map<String, Any>>>
    
    /**
     * Update student profile
     * PATCH /students/:id
     */
    @PATCH("students/{id}")
    suspend fun updateStudent(
        @Path("id") studentId: String,
        @Body updates: Map<String, @JvmSuppressWildcards Any>
    ): Response<ApiResponse<Student>>

    /**
     * Get test attempts for a student (used by parent portal)
     * GET /students/:studentId/test-attempts
     */
    @GET("students/{studentId}/test-attempts")
    suspend fun getChildTestAttempts(
        @Path("studentId") studentId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<ChildTestResults>>
    
    /**
     * Upload avatar image URL
     * PATCH /user/avatar
     */
    @PATCH("user/avatar")
    suspend fun uploadAvatar(
        @Body request: Map<String, String>
    ): Response<ApiResponse<Map<String, Any>>>
    
    /**
     * Upload file (multipart)
     * POST /files
     * Returns Cloudinary url and public_id wrapped in ApiResponse
     */
    @Multipart
    @POST("files")
    suspend fun uploadFile(
        @Part file: okhttp3.MultipartBody.Part,
        @Part("path") path: okhttp3.RequestBody? = null
    ): Response<ApiResponse<Map<String, Any>>>
    
    // =====================================================
    // PARENTS
    // =====================================================
    
    /**
     * Get parent by ID
     * GET /parents/:id
     */
    @GET("parents/{id}")
    suspend fun getParent(
        @Path("id") parentId: String
    ): Response<ApiResponse<Parent>>
    
    /**
     * Update parent profile
     * PATCH /parents/:id
     */
    @PATCH("parents/{id}")
    suspend fun updateParent(
        @Path("id") parentId: String,
        @Body updates: Map<String, Any>
    ): Response<ApiResponse<Parent>>
    
    // =====================================================
    // CLASSES
    // =====================================================
    
    /**
     * Get all classes (paginated)
     * GET /classes
     */
    @GET("classes")
    suspend fun getClasses(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("filters") filters: String? = null
    ): Response<ApiResponse<PaginatedResponse<ClassInfo>>>
    
    /**
     * Get public classes
     * GET /classes/public
     */
    @GET("classes/public")
    suspend fun getPublicClasses(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<ApiResponse<PaginatedResponse<ClassInfo>>>
    
    /**
     * Get class by ID
     * GET /classes/:id
     */
    @GET("classes/{id}")
    suspend fun getClass(
        @Path("id") classId: String
    ): Response<ApiResponse<ClassInfo>>
    
    /**
     * Get class banner info
     * GET /classes/:id/banner-info
     */
    @GET("classes/{id}/banner-info")
    suspend fun getClassBannerInfo(
        @Path("id") classId: String
    ): Response<ApiResponse<ClassInfo>>
    
    // =====================================================
    // SESSIONS (ATTENDANCE)
    // =====================================================
    
    /**
     * Get today's session for a class
     * GET /sessions/today/:classId
     */
    @GET("sessions/today/{classId}")
    suspend fun getTodaySession(
        @Path("classId") classId: String
    ): Response<ApiResponse<Session>>
    
    /**
     * Get student attendance history
     * GET /sessions/student/:studentId
     * Returns pre-calculated attendance stats from backend
     */
    @GET("sessions/student/{studentId}")
    suspend fun getStudentAttendance(
        @Path("studentId") studentId: String
    ): Response<ApiResponse<StudentAttendanceResponse>>
    
    /**
     * Get all sessions for a class
     * GET /sessions/all/:classId
     */
    @GET("sessions/all/{classId}")
    suspend fun getClassSessions(
        @Path("classId") classId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<ApiResponse<PaginatedResponse<Session>>>
    
    // =====================================================
    // PAYMENTS
    // =====================================================
    
    /**
     * Get payments by student ID
     * GET /payments/students/:studentId
     */
    @GET("payments/students/{studentId}")
    suspend fun getStudentPayments(
        @Path("studentId") studentId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<ApiResponse<PaginatedResponse<Payment>>>
    
    /**
     * Get QR code for payment
     * GET /payments/qrcode
     */
    @GET("payments/qrcode")
    suspend fun getPaymentQRCode(
        @Query("paymentId") paymentId: String,
        @Query("amount") amount: Double
    ): Response<ApiResponse<QRCodeResponse>>
    
    // =====================================================
    // DASHBOARD
    // =====================================================
    
    /**
     * Get dashboard statistics
     * GET /dashboard
     */
    @GET("dashboard")
    suspend fun getDashboard(): Response<ApiResponse<Map<String, Any>>>
    
    // =====================================================
    // NOTIFICATIONS
    // =====================================================
    
    /**
     * Get user notifications (paginated)
     * GET /notifications
     */
    @GET("notifications")
    suspend fun getNotifications(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("isRead") isRead: Boolean? = null
    ): Response<ApiResponse<NotificationListResponse>>
    
    /**
     * Get unread notification count
     * GET /notifications/unread-count
     */
    @GET("notifications/unread-count")
    suspend fun getUnreadCount(): Response<ApiResponse<UnreadCountResponse>>
    
    /**
     * Mark a notification as read
     * PATCH /notifications/:id/read
     */
    @PATCH("notifications/{id}/read")
    suspend fun markNotificationAsRead(
        @Path("id") notificationId: String
    ): Response<ApiResponse<Notification>>
    
    /**
     * Mark all notifications as read
     * PATCH /notifications/read-all
     */
    @PATCH("notifications/read-all")
    suspend fun markAllNotificationsAsRead(): Response<ApiResponse<Map<String, Any>>>
    
    /**
     * Delete a notification
     * DELETE /notifications/:id
     */
    @DELETE("notifications/{id}")
    suspend fun deleteNotification(
        @Path("id") notificationId: String
    ): Response<ApiResponse<Map<String, Any>>>
    
    // =====================================================
    // TESTS (Student)
    // =====================================================
    
    /**
     * Get available tests for logged-in student
     * GET /tests/student/available
     */
    @GET("tests/student/available")
    suspend fun getAvailableTests(): Response<ApiResponse<List<Test>>>
    
    /**
     * Get test for student (without correct answers)
     * GET /tests/student/:id
     */
    @GET("tests/student/{id}")
    suspend fun getTestForStudent(
        @Path("id") testId: String
    ): Response<ApiResponse<Test>>
    
    /**
     * Submit test answers
     * POST /tests/:id/submit
     */
    @POST("tests/{id}/submit")
    suspend fun submitTest(
        @Path("id") testId: String,
        @Body request: SubmitTestRequest
    ): Response<ApiResponse<TestAttempt>>
    
    /**
     * Submit writing test
     * POST /tests/:id/submit/writing
     */
    @POST("tests/{id}/submit/writing")
    suspend fun submitWriting(
        @Path("id") testId: String,
        @Body request: SubmitWritingRequest
    ): Response<ApiResponse<TestAttempt>>
    
    /**
     * Submit speaking test (audio file)
     * POST /tests/:id/submit/speaking
     */
    @Multipart
    @POST("tests/{id}/submit/speaking")
    suspend fun submitSpeaking(
        @Path("id") testId: String,
        @Part audio: okhttp3.MultipartBody.Part
    ): Response<ApiResponse<TestAttempt>>
    
    /**
     * Get student's attempt history
     * GET /tests/student/attempts
     */
    @GET("tests/student/attempts")
    suspend fun getMyAttempts(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<ApiResponse<PaginatedResponse<TestAttempt>>>
    
    /**
     * Get attempt result by ID
     * GET /tests/attempts/:id
     */
    @GET("tests/attempts/{id}")
    suspend fun getAttemptResult(
        @Path("id") attemptId: String
    ): Response<ApiResponse<TestAttempt>>
    
    // =====================================================
    // FORGOT PASSWORD
    // =====================================================
    
    /**
     * Send password reset email
     * POST /auth/send-request-password
     */
    @POST("auth/send-request-password")
    suspend fun sendPasswordResetRequest(
        @Body request: Map<String, String>
    ): Response<ApiResponse<Unit>>
    
    /**
     * Reset password with OTP code
     * PATCH /auth/reset-password
     */
    @PATCH("auth/reset-password")
    suspend fun resetPassword(
        @Body request: Map<String, String>
    ): Response<ApiResponse<Unit>>
    
    // =====================================================
    // ADVERTISEMENTS
    // =====================================================
    
    /**
     * Get advertisement banners
     * GET /advertisements/banners/:limit
     */
    @GET("advertisements/banners/{limit}")
    suspend fun getAdvertisementBanners(
        @Path("limit") limit: Int = 5
    ): Response<ApiResponse<List<Advertisement>>>
    
    // =====================================================
    // CHATBOT
    // =====================================================
    
    /**
     * Send message to AI chatbot
     * POST /chatbot/send
     */
    @POST("chatbot/send")
    suspend fun sendChatMessage(
        @Body request: Map<String, @JvmSuppressWildcards Any>
    ): Response<Map<String, Any>>
    
    // =====================================================
    // REGISTRATIONS (Course Registration)
    // =====================================================
    
    /**
     * Submit a course registration (public - no auth required)
     * POST /registrations
     */
    @POST("registrations")
    suspend fun submitRegistration(
        @Body request: CreateRegistrationRequest
    ): Response<ApiResponse<Registration>>
    
    // =====================================================
    // FCM DEVICE TOKEN
    // =====================================================
    
    /**
     * Register FCM device token for push notifications
     * POST /notifications/register-device
     */
    @POST("notifications/register-device")
    suspend fun registerDeviceToken(
        @Body request: Map<String, String>
    ): Response<ApiResponse<Unit>>

    // =====================================================
    // LEARNING MATERIALS
    // =====================================================

    /**
     * Get learning materials by class
     * GET /materials?classId=&category=&page=&limit=
     */
    @GET("materials")
    suspend fun getMaterials(
        @Query("classId") classId: String,
        @Query("category") category: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<ApiResponse<MaterialsResponse>>

    // =====================================================
    // DICTATION PRACTICE (TTS)
    // =====================================================

    /**
     * Get a random dictation sentence metadata (text is hidden)
     * GET /tts/dictation/random?level=easy|medium|hard
     */
    @GET("tts/dictation/random")
    suspend fun getDictationRandom(
        @Query("level") level: String? = null
    ): Response<ApiResponse<DictationSentence>>

    /**
     * Generate TTS audio for a dictation sentence
     * POST /tts/dictation/audio
     * Returns binary WAV audio
     */
    @POST("tts/dictation/audio")
    suspend fun getDictationAudio(
        @Body request: DictationAudioRequest
    ): Response<okhttp3.ResponseBody>

    /**
     * Check user's dictation answer
     * POST /tts/dictation/check
     */
    @POST("tts/dictation/check")
    suspend fun checkDictation(
        @Body request: DictationCheckRequest
    ): Response<ApiResponse<DictationResult>>
}
