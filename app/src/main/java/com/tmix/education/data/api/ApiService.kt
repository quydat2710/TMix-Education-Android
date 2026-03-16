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
        @Body updates: Map<String, Any>
    ): Response<ApiResponse<Student>>
    
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
     */
    @GET("sessions/student/{studentId}")
    suspend fun getStudentAttendance(
        @Path("studentId") studentId: String
    ): Response<ApiResponse<List<Session>>>
    
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
    // NOTIFICATIONS (if available)
    // =====================================================
    
    /**
     * Get user notifications
     * This endpoint may need to be added to Backend
     */
    @GET("notifications")
    suspend fun getNotifications(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<PaginatedResponse<Map<String, Any>>>>
    
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
    ): Response<ApiResponse<List<TestAttempt>>>
    
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
}
