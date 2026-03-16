package com.tmix.education.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Navigation routes
 */
sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    
    // Student
    data object StudentDashboard : Screen("student/dashboard")
    data object StudentClasses : Screen("student/classes")
    data object StudentSchedule : Screen("student/schedule")
    data object StudentTests : Screen("student/tests")
    data object StudentProfile : Screen("student/profile")
    
    // Student Detail Screens
    data object ClassDetail : Screen("student/class/{classId}") {
        fun createRoute(classId: String) = "student/class/$classId"
    }
    data object TestTaking : Screen("student/test/{testId}") {
        fun createRoute(testId: String) = "student/test/$testId"
    }
    
    // Parent
    data object ParentDashboard : Screen("parent/dashboard")
    data object ParentChildren : Screen("parent/children")
    data object ParentSchedule : Screen("parent/schedule")
    data object ParentPayments : Screen("parent/payments")
    data object ParentProfile : Screen("parent/profile")
    
    // Shared
    data object Notifications : Screen("notifications")
    data object EditProfile : Screen("profile/edit")
    data object ChangePassword : Screen("profile/password")
    data object HelpCenter : Screen("help")
    data object ForgotPassword : Screen("forgot-password")
    
    // Parent Detail
    data object ParentChildDetail : Screen("parent/child/{childId}") {
        fun createRoute(childId: String) = "parent/child/$childId"
    }
}

/**
 * Bottom navigation items
 */
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    // Student
    data object StudentHome : BottomNavItem(
        Screen.StudentDashboard.route, "Trang chủ",
        Icons.Filled.Home, Icons.Outlined.Home
    )
    data object StudentClasses : BottomNavItem(
        Screen.StudentClasses.route, "Lớp học",
        Icons.Filled.MenuBook, Icons.Outlined.MenuBook
    )
    data object StudentSchedule : BottomNavItem(
        Screen.StudentSchedule.route, "Lịch học",
        Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth
    )
    data object StudentTests : BottomNavItem(
        Screen.StudentTests.route, "Kiểm tra",
        Icons.Filled.Quiz, Icons.Outlined.Quiz
    )
    data object StudentProfile : BottomNavItem(
        Screen.StudentProfile.route, "Tài khoản",
        Icons.Filled.Person, Icons.Outlined.Person
    )
    
    // Parent
    data object ParentHome : BottomNavItem(
        Screen.ParentDashboard.route, "Trang chủ",
        Icons.Filled.Home, Icons.Outlined.Home
    )
    data object ParentChildren : BottomNavItem(
        Screen.ParentChildren.route, "Con của tôi",
        Icons.Filled.People, Icons.Outlined.People
    )
    data object ParentPayments : BottomNavItem(
        Screen.ParentPayments.route, "Học phí",
        Icons.Filled.Payment, Icons.Outlined.Payment
    )
    data object ParentSchedule : BottomNavItem(
        Screen.ParentSchedule.route, "Lịch học",
        Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth
    )
    data object ParentProfile : BottomNavItem(
        Screen.ParentProfile.route, "Tài khoản",
        Icons.Filled.Person, Icons.Outlined.Person
    )
    
    companion object {
        val studentItems = listOf(StudentHome, StudentClasses, StudentSchedule, StudentTests, StudentProfile)
        val parentItems = listOf(ParentHome, ParentChildren, ParentSchedule, ParentPayments, ParentProfile)
    }
}
