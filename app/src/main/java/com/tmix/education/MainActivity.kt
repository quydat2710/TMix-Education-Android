package com.tmix.education

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.tmix.education.navigation.AppNavigation
import com.tmix.education.ui.theme.LocalThemeManager
import com.tmix.education.ui.theme.TMixEducationTheme
import com.tmix.education.ui.theme.ThemeManager
import com.tmix.education.ui.theme.LanguageManager
import com.tmix.education.ui.theme.LocalLanguageManager

class MainActivity : ComponentActivity() {

    // Permission request launcher for Android 13+ notifications
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            val context = androidx.compose.ui.platform.LocalContext.current
            val themeManager = remember { ThemeManager() }
            val isDarkMode by themeManager.isDarkMode.collectAsState()
            
            val languageManager = remember { LanguageManager(applicationContext) }
            val currentLanguage by languageManager.currentLanguage.collectAsState()
            
            // Create a localized context using the current language state
            val localizedContext = remember(currentLanguage) {
                LanguageManager.createLocaleContext(context, currentLanguage)
            }
            
            CompositionLocalProvider(
                LocalThemeManager provides themeManager,
                LocalLanguageManager provides languageManager,
                androidx.compose.ui.platform.LocalContext provides localizedContext
            ) {
                TMixEducationTheme(darkTheme = isDarkMode) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavigation()
                    }
                }
            }
        }
    }
}