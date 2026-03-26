package com.tmix.education.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * TMIX Education Design System - Colors
 * Based on official TMIX logo: Navy (#1E3A5F) + Red (#E31837)
 */

// ========== Primary Colors (from logo) ==========
val TMixNavy = Color(0xFF1E3A5F)        // Primary - Navy Blue
val TMixRed = Color(0xFFE31837)          // Accent - TMIX Red

// ========== Extended Palette ==========
val TMixNavyDark = Color(0xFF0F1E33)     // Dark variant
val TMixNavyLight = Color(0xFF2E4A6F)    // Light variant
val TMixNavySoft = Color(0xFF3D5A80)     // Softer variant (for gradients)
val TMixRedDark = Color(0xFFB31228)      // Dark variant
val TMixRedLight = Color(0xFFFF3A4F)     // Light variant
val TMixRedSoft = Color(0xFFFF6B7A)      // Softer variant

// ========== Neutral Colors ==========
val White = Color(0xFFFFFFFF)
val Background = Color(0xFFF8FAFC)       // Slightly off-white
val Surface = Color(0xFFFFFFFF)
val SurfaceVariant = Color(0xFFF1F5F9)

// ========== Text Colors ==========
val TextPrimary = Color(0xFF1E293B)      // Dark slate
val TextSecondary = Color(0xFF64748B)    // Muted
val TextTertiary = Color(0xFF94A3B8)     // Placeholder/disabled
val TextOnPrimary = Color(0xFFFFFFFF)

// ========== Status Colors ==========
val Success = Color(0xFF22C55E)
val SuccessLight = Color(0xFFDCFCE7)
val Warning = Color(0xFFF59E0B)
val WarningLight = Color(0xFFFEF3C7)
val Error = Color(0xFFEF4444)
val ErrorLight = Color(0xFFFEE2E2)
val Info = Color(0xFF3B82F6)
val InfoLight = Color(0xFFDBEAFE)

// ========== Shimmer / Loading ==========
val ShimmerBase = Color(0xFFE2E8F0)
val ShimmerHighlight = Color(0xFFF8FAFC)

// ========== Gradient Brushes (brand-consistent) ==========
val GradientNavy = Brush.linearGradient(listOf(TMixNavy, TMixNavySoft))
val GradientNavyVertical = Brush.verticalGradient(listOf(TMixNavy, TMixNavyLight))