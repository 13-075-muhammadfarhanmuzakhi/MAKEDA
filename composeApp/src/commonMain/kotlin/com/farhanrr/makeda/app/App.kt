package com.farhanrr.makeda.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.farhanrr.makeda.screen.CalendarScreen
import com.farhanrr.makeda.screen.HomeScreen
import com.farhanrr.makeda.screen.LoginScreen
import com.farhanrr.makeda.screen.ProfileScreen
import com.farhanrr.makeda.screen.RegisterScreen
import com.farhanrr.makeda.screen.ReportScreen
import com.farhanrr.makeda.screen.SplashScreen
import com.farhanrr.makeda.theme.MakedaTheme
import com.farhanrr.makeda.viewmodel.MakedaViewModel

private enum class AuthRoute { LOGIN, REGISTER }
private enum class RootRoute { SPLASH, AUTH, MAIN }

enum class MakedaTab(val label: String) {
    HOME("Beranda"),
    CALENDAR("Kalender"),
    REPORT("Laporan"),
    PROFILE("Profil")
}

@Composable
fun App() {
    val viewModel: MakedaViewModel = viewModel { MakedaViewModel() }
    val state by viewModel.uiState.collectAsState()

    MakedaTheme(darkMode = state.settings.darkMode) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {

            var rootRoute by remember { mutableStateOf(RootRoute.SPLASH) }
            var authRoute by remember { mutableStateOf(AuthRoute.LOGIN) }

            AnimatedContent(
                targetState = if (rootRoute == RootRoute.MAIN || state.isLoggedIn) "main" else rootRoute.name,
                transitionSpec = {
                    (fadeIn(tween(420)) + androidx.compose.animation.scaleIn(initialScale = 0.96f, animationSpec = tween(420)))
                        .togetherWith(fadeOut(tween(220)))
                },
                label = "root-transition"
            ) { route ->
                when {
                    route == "SPLASH" -> SplashScreen(onFinished = { rootRoute = RootRoute.AUTH })
                    route == "main" -> MainScaffold(viewModel = viewModel, onLogout = {
                        viewModel.logout()
                        rootRoute = RootRoute.AUTH
                    })
                    else -> {
                        AnimatedContent(
                            targetState = authRoute,
                            transitionSpec = {
                                if (targetState == AuthRoute.REGISTER) {
                                    (androidx.compose.animation.slideInHorizontally(animationSpec = tween(380)) { it / 3 } + fadeIn(tween(380)))
                                        .togetherWith(fadeOut(tween(180)))
                                } else {
                                    (androidx.compose.animation.slideInHorizontally(animationSpec = tween(380)) { -it / 3 } + fadeIn(tween(380)))
                                        .togetherWith(fadeOut(tween(180)))
                                }
                            },
                            label = "auth-transition"
                        ) { auth ->
                            when (auth) {
                                AuthRoute.LOGIN -> LoginScreen(
                                    viewModel = viewModel,
                                    onGoRegister = { viewModel.clearAuthError(); authRoute = AuthRoute.REGISTER }
                                )
                                AuthRoute.REGISTER -> RegisterScreen(
                                    viewModel = viewModel,
                                    onGoLogin = { viewModel.clearAuthError(); authRoute = AuthRoute.LOGIN }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MainScaffold(viewModel: MakedaViewModel, onLogout: () -> Unit) {
    var currentTab by remember { mutableStateOf(MakedaTab.HOME) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentTab == MakedaTab.HOME,
                    onClick = { currentTab = MakedaTab.HOME },
                    icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                    label = { Text(MakedaTab.HOME.label) }
                )
                NavigationBarItem(
                    selected = currentTab == MakedaTab.CALENDAR,
                    onClick = { currentTab = MakedaTab.CALENDAR },
                    icon = { Icon(Icons.Filled.CalendarMonth, contentDescription = null) },
                    label = { Text(MakedaTab.CALENDAR.label) }
                )
                NavigationBarItem(
                    selected = currentTab == MakedaTab.REPORT,
                    onClick = { currentTab = MakedaTab.REPORT },
                    icon = { Icon(Icons.Filled.BarChart, contentDescription = null) },
                    label = { Text(MakedaTab.REPORT.label) }
                )
                NavigationBarItem(
                    selected = currentTab == MakedaTab.PROFILE,
                    onClick = { currentTab = MakedaTab.PROFILE },
                    icon = { Icon(Icons.Filled.AccountCircle, contentDescription = null) },
                    label = { Text(MakedaTab.PROFILE.label) }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    (fadeIn(tween(300)) + androidx.compose.animation.slideInVertically(animationSpec = tween(300)) { it / 12 })
                        .togetherWith(fadeOut(tween(150)))
                },
                label = "tab-transition"
            ) { tab ->
                when (tab) {
                    MakedaTab.HOME -> HomeScreen(viewModel = viewModel)
                    MakedaTab.CALENDAR -> CalendarScreen(viewModel = viewModel)
                    MakedaTab.REPORT -> ReportScreen(viewModel = viewModel)
                    MakedaTab.PROFILE -> ProfileScreen(viewModel = viewModel, onLogout = onLogout)
                }
            }
        }
    }
}
