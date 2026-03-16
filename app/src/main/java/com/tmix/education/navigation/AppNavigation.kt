package com.tmix.education.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tmix.education.ui.screens.*
import com.tmix.education.ui.viewmodel.LoginViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val loginViewModel: LoginViewModel = viewModel()
    val currentUser = loginViewModel.getCurrentUser()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // Hide bottom bar on these screens
    val hideBottomBarRoutes = listOf(
        "splash", "login", "forgot-password",
        "student/class/", "student/test/",
        "parent/child/",
        "notifications", "profile/edit", "profile/password", "help"
    )
    val showBottomBar = (currentDestination?.route?.startsWith("student/") == true ||
                        currentDestination?.route?.startsWith("parent/") == true) &&
                        hideBottomBarRoutes.none { currentDestination?.route?.contains(it) == true }
    
    val isStudentUser = currentUser?.isStudent ?: true
    val bottomNavItems = if (isStudentUser) BottomNavItem.studentItems else BottomNavItem.parentItems
    
    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            },
                            label = { 
                                Text(
                                    text = item.title,
                                    maxLines = 1,
                                    softWrap = false,
                                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall
                                ) 
                            },
                            selected = selected,
                            alwaysShowLabel = true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Splash Screen - checks auth state
            composable(Screen.Splash.route) {
                SplashScreen(
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToStudentDashboard = {
                        navController.navigate(Screen.StudentDashboard.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToParentDashboard = {
                        navController.navigate(Screen.ParentDashboard.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }
            
            // Login
            composable(
                Screen.Login.route,
                enterTransition = { fadeIn(animationSpec = tween(300)) },
                exitTransition = { fadeOut(animationSpec = tween(300)) }
            ) {
                LoginScreen(
                    onLoginSuccess = { isStudentRole ->
                        val destination = if (isStudentRole) 
                            Screen.StudentDashboard.route 
                        else 
                            Screen.ParentDashboard.route
                        navController.navigate(destination) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onForgotPassword = {
                        navController.navigate(Screen.ForgotPassword.route)
                    }
                )
            }
            
            // ===== STUDENT SCREENS =====
            composable(Screen.StudentDashboard.route) { 
                StudentDashboardScreen(
                    onNotificationClick = { navController.navigate(Screen.Notifications.route) },
                    onClassClick = { classId -> navController.navigate(Screen.ClassDetail.createRoute(classId)) }
                )
            }
            
            composable(Screen.StudentClasses.route) { 
                StudentClassesScreen(
                    onClassClick = { classId -> navController.navigate(Screen.ClassDetail.createRoute(classId)) }
                )
            }
            
            composable(Screen.StudentSchedule.route) { StudentScheduleScreen() }
            
            composable(Screen.StudentTests.route) { 
                StudentTestsScreen(
                    onStartTest = { testId -> navController.navigate(Screen.TestTaking.createRoute(testId)) }
                )
            }
            
            composable(Screen.StudentProfile.route) { 
                ProfileScreen(
                    isStudent = true,
                    user = currentUser,
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onEditProfile = { navController.navigate(Screen.EditProfile.route) },
                    onChangePassword = { navController.navigate(Screen.ChangePassword.route) },
                    onHelpCenter = { navController.navigate(Screen.HelpCenter.route) }
                )
            }
            
            // Class Detail
            composable(
                Screen.ClassDetail.route,
                arguments = listOf(navArgument("classId") { type = NavType.StringType })
            ) { backStackEntry ->
                val classId = backStackEntry.arguments?.getString("classId") ?: ""
                ClassDetailScreen(
                    classId = classId,
                    onBack = { navController.popBackStack() }
                )
            }
            
            // Test Taking
            composable(
                Screen.TestTaking.route,
                arguments = listOf(navArgument("testId") { type = NavType.StringType })
            ) { backStackEntry ->
                val testId = backStackEntry.arguments?.getString("testId") ?: ""
                TestTakingScreen(
                    testId = testId,
                    onBack = { navController.popBackStack() },
                    onSubmit = { navController.popBackStack() }
                )
            }
            
            // ===== PARENT SCREENS =====
            composable(Screen.ParentDashboard.route) { 
                ParentDashboardScreen(
                    onNotificationClick = { navController.navigate(Screen.Notifications.route) },
                    onChildClick = { navController.navigate(Screen.ParentChildren.route) },
                    onPaymentClick = { navController.navigate(Screen.ParentPayments.route) }
                )
            }
            
            composable(Screen.ParentChildren.route) { 
                ParentChildrenScreen(
                    onChildClick = { childId -> navController.navigate(Screen.ParentChildDetail.createRoute(childId)) },
                    onChildSchedule = { navController.navigate(Screen.StudentSchedule.route) },
                    onChildPayment = { navController.navigate(Screen.ParentPayments.route) }
                )
            }
            
            composable(Screen.ParentPayments.route) { ParentPaymentsScreen() }
            
            composable(Screen.ParentSchedule.route) { StudentScheduleScreen() }
            
            composable(Screen.ParentProfile.route) { 
                ProfileScreen(
                    isStudent = false,
                    user = currentUser,
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onEditProfile = { navController.navigate(Screen.EditProfile.route) },
                    onChangePassword = { navController.navigate(Screen.ChangePassword.route) },
                    onHelpCenter = { navController.navigate(Screen.HelpCenter.route) }
                )
            }
            
            // ===== SHARED SCREENS =====
            composable(Screen.Notifications.route) {
                NotificationsScreen(onBack = { navController.popBackStack() })
            }
            
            composable(Screen.EditProfile.route) {
                EditProfileScreen(
                    user = currentUser,
                    onBack = { navController.popBackStack() },
                    onSave = { navController.popBackStack() }
                )
            }
            
            composable(Screen.ChangePassword.route) {
                ChangePasswordScreen(
                    onBack = { navController.popBackStack() },
                    onSuccess = { navController.popBackStack() }
                )
            }
            
            composable(Screen.HelpCenter.route) {
                HelpCenterScreen(onBack = { navController.popBackStack() })
            }
            
            // Forgot Password
            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(
                    onBack = { navController.popBackStack() },
                    onSuccess = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.ForgotPassword.route) { inclusive = true }
                        }
                    }
                )
            }
            
            // Parent Child Detail
            composable(
                Screen.ParentChildDetail.route,
                arguments = listOf(navArgument("childId") { type = NavType.StringType })
            ) { backStackEntry ->
                val childId = backStackEntry.arguments?.getString("childId") ?: ""
                ParentChildDetailScreen(
                    childId = childId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
