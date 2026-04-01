package com.tmix.education.data.api

import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Utility to parse API error responses into user-friendly Vietnamese messages.
 *
 * Backend always returns: { statusCode: Int, message: String, data: Any? }
 * On error, the message is in the errorBody(), not in body().
 */
object ApiErrorParser {

    private val gson = Gson()

    /**
     * Parse error from a Retrofit Response.
     * Tries to extract the `message` field from the JSON error body.
     * Falls back to HTTP status text defaults in Vietnamese.
     */
    fun <T> parse(response: Response<T>): String {
        // 1. Try to parse the error body JSON
        try {
            val errorBodyString = response.errorBody()?.string()
            if (!errorBodyString.isNullOrBlank()) {
                val jsonObject = gson.fromJson(errorBodyString, JsonObject::class.java)
                val message = jsonObject?.get("message")?.asString
                if (!message.isNullOrBlank()) {
                    return message
                }
            }
        } catch (_: Exception) {
            // JSON parsing failed, fall through to defaults
        }

        // 2. Try the body's message (for non-error status codes where body is still parsed)
        try {
            val body = response.body()
            if (body != null) {
                val bodyJson = gson.toJsonTree(body).asJsonObject
                val message = bodyJson?.get("message")?.asString
                if (!message.isNullOrBlank() && message != "Success") {
                    return message
                }
            }
        } catch (_: Exception) { }

        // 3. Map HTTP status code to Vietnamese default
        return mapHttpCode(response.code())
    }

    /**
     * Parse an Exception into a user-friendly Vietnamese message.
     */
    fun parseException(e: Throwable): String {
        return when (e) {
            is UnknownHostException -> "Không có kết nối mạng. Vui lòng kiểm tra WiFi/4G."
            is ConnectException -> "Không thể kết nối đến máy chủ. Vui lòng thử lại."
            is SocketTimeoutException -> "Kết nối đã hết thời gian. Vui lòng thử lại."
            is java.io.IOException -> "Lỗi kết nối mạng. Vui lòng thử lại."
            else -> e.message?.takeIf { it.isNotBlank() } ?: "Đã xảy ra lỗi không xác định"
        }
    }

    /**
     * Map HTTP status codes to Vietnamese messages
     */
    private fun mapHttpCode(code: Int): String = when (code) {
        400 -> "Thông tin không hợp lệ. Vui lòng kiểm tra lại."
        401 -> "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại."
        403 -> "Bạn không có quyền truy cập chức năng này."
        404 -> "Không tìm thấy dữ liệu yêu cầu."
        409 -> "Dữ liệu bị trùng lặp. Vui lòng kiểm tra lại."
        422 -> "Dữ liệu không hợp lệ. Vui lòng kiểm tra lại."
        429 -> "Bạn thao tác quá nhanh. Vui lòng đợi một lát."
        500 -> "Lỗi máy chủ. Vui lòng thử lại sau."
        502, 503 -> "Hệ thống đang bảo trì. Vui lòng thử lại sau."
        else -> "Đã xảy ra lỗi (mã $code). Vui lòng thử lại."
    }
}
