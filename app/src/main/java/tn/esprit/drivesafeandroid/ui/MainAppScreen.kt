package tn.esprit.drivesafeandroid.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

sealed class AppScreen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Simulator : AppScreen("simulator", "Simulator", Icons.Default.Home)
    object History : AppScreen("history", "History", Icons.Default.DateRange)
    object Dashboard : AppScreen("dashboard", "Dashboard", Icons.Default.Info)
}

@Composable
fun MainAppScreen() {
    val viewModel = remember { LwmsViewModel() }
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Simulator) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor = Color(0xFF2563EB)
            ) {
                listOf(AppScreen.Simulator, AppScreen.History, AppScreen.Dashboard).forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title, fontWeight = FontWeight.Bold) },
                        selected = currentScreen == screen,
                        onClick = { currentScreen = screen },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF2563EB),
                            unselectedIconColor = Color(0xFF64748B),
                            selectedTextColor = Color(0xFF2563EB),
                            unselectedTextColor = Color(0xFF64748B),
                            indicatorColor = Color(0xFFDBE1FF)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (currentScreen) {
                AppScreen.Simulator -> SimulatorScreen(viewModel)
                AppScreen.History -> HistoryScreen()
                AppScreen.Dashboard -> DashboardScreen()
            }
        }
    }
}
