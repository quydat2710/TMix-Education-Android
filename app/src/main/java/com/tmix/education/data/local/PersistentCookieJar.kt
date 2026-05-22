package com.tmix.education.data.local

import android.content.Context
import android.content.SharedPreferences
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

/**
 * Persistent CookieJar that stores cookies in SharedPreferences.
 * Ensures refresh_token cookie survives app restarts so the user
 * doesn't have to re-login every time.
 */
class PersistentCookieJar(context: Context) : CookieJar {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("tmix_cookies", Context.MODE_PRIVATE)

    // In-memory cache for fast access
    private val cookieStore = mutableMapOf<String, MutableList<Cookie>>()

    init {
        // Load persisted cookies on init
        loadFromPrefs()
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val host = url.host
        cookieStore[host] = cookies.toMutableList()
        persistToPrefs(host, cookies)
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val host = url.host
        val cookies = cookieStore[host] ?: return emptyList()

        // Filter out expired cookies
        val now = System.currentTimeMillis() / 1000
        val validCookies = cookies.filter { it.expiresAt / 1000 > now }

        // Update store if any were removed
        if (validCookies.size != cookies.size) {
            cookieStore[host] = validCookies.toMutableList()
            persistToPrefs(host, validCookies)
        }

        return validCookies
    }

    /**
     * Clear all cookies (on logout)
     */
    fun clear() {
        cookieStore.clear()
        prefs.edit().clear().apply()
    }

    private fun persistToPrefs(host: String, cookies: List<Cookie>) {
        val serialized = cookies.map { encodeCookie(it) }.toSet()
        prefs.edit().putStringSet("cookies_$host", serialized).apply()
    }

    private fun loadFromPrefs() {
        prefs.all.forEach { (key, value) ->
            if (key.startsWith("cookies_") && value is Set<*>) {
                val host = key.removePrefix("cookies_")
                val cookies = value.mapNotNull { str ->
                    decodeCookie(host, str as? String ?: return@mapNotNull null)
                }.toMutableList()
                if (cookies.isNotEmpty()) {
                    cookieStore[host] = cookies
                }
            }
        }
    }

    /**
     * Encode a Cookie to a storable string format.
     * Format: name|value|domain|path|expiresAt|secure|httpOnly
     */
    private fun encodeCookie(cookie: Cookie): String {
        return listOf(
            cookie.name,
            cookie.value,
            cookie.domain,
            cookie.path,
            cookie.expiresAt.toString(),
            cookie.secure.toString(),
            cookie.httpOnly.toString()
        ).joinToString("|")
    }

    /**
     * Decode a Cookie from the stored string format.
     */
    private fun decodeCookie(host: String, encoded: String): Cookie? {
        return try {
            val parts = encoded.split("|")
            if (parts.size < 7) return null

            Cookie.Builder()
                .name(parts[0])
                .value(parts[1])
                .domain(parts[2])
                .path(parts[3])
                .expiresAt(parts[4].toLong())
                .apply {
                    if (parts[5].toBoolean()) secure()
                    if (parts[6].toBoolean()) httpOnly()
                }
                .build()
        } catch (e: Exception) {
            null
        }
    }
}
