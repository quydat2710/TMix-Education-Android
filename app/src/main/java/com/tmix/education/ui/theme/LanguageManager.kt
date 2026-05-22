package com.tmix.education.ui.theme

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import androidx.compose.runtime.staticCompositionLocalOf

val LocalLanguageManager = staticCompositionLocalOf<LanguageManager> {
    error("No LanguageManager provided")
}

class LanguageManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("tmix_language_prefs", Context.MODE_PRIVATE)

    // "vi" for Vietnamese (Default), "en" for English
    private val _currentLanguage = MutableStateFlow(prefs.getString("app_lang", "vi") ?: "vi")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    fun setLanguage(langCode: String) {
        prefs.edit().putString("app_lang", langCode).apply()
        _currentLanguage.value = langCode
    }

    companion object {
        fun createLocaleContext(context: Context, languageCode: String): Context {
            val locale = Locale(languageCode)
            Locale.setDefault(locale)
            
            val configuration = Configuration(context.resources.configuration)
            configuration.setLocale(locale)
            configuration.setLayoutDirection(locale)
            
            val newContext = context.createConfigurationContext(configuration)
            
            // Return a custom ContextWrapper so that Compose can unwrap it to find the Activity
            // ContextImpl (returned by createConfigurationContext) cannot be unwrapped.
            return object : android.content.ContextWrapper(context) {
                override fun getResources(): android.content.res.Resources {
                    return newContext.resources
                }
            }
        }
    }
}
