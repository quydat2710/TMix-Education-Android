package com.tmix.education

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.tmix.education.data.api.ApiConfig
import com.tmix.education.util.NotificationHelper

/**
 * TMIX Education Application
 * Initialize API configuration + Coil image caching on app start
 */
class TMixApplication : Application(), ImageLoaderFactory {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize API configuration with application context
        ApiConfig.init(this, isProduction = false)
        
        // Create notification channel for system notifications
        NotificationHelper.createNotificationChannel(this)
    }

    /**
     * Coil ImageLoader with memory + disk cache
     * Memory: 25% of available heap (~32-64MB)
     * Disk: 50MB persistent cache
     */
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("coil_cache"))
                    .maxSizeBytes(50L * 1024 * 1024)
                    .build()
            }
            .crossfade(200)
            .respectCacheHeaders(false) // Always cache regardless of headers
            .build()
    }
}
