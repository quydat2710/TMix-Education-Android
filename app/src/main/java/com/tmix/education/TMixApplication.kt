package com.tmix.education

import android.app.Application
import com.tmix.education.data.api.ApiConfig

/**
 * TMIX Education Application
 * Initialize API configuration on app start
 */
class TMixApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize API configuration with application context
        ApiConfig.init(this, isProduction = false)
    }
}
