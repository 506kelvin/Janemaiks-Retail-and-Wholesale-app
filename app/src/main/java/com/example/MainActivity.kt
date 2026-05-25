package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ShopViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: ShopViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkTheme by viewModel.isDarkMode.collectAsStateWithLifecycle()
            val loggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()

            MyApplicationTheme(darkTheme = isDarkTheme) {
                if (!loggedIn) {
                    LoginScreen(viewModel = viewModel) {
                        // Success callback handled automatically by state
                    }
                } else {
                    var currentRoute by remember { mutableStateOf("dashboard") }

                    AppNavigationWrapper(
                        currentRoute = currentRoute,
                        onNavigate = { currentRoute = it },
                        viewModel = viewModel
                    ) {
                        AnimatedContent(
                            targetState = currentRoute,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "screen_transitions"
                        ) { targetRoute ->
                            when (targetRoute) {
                                "dashboard" -> DashboardScreen(viewModel = viewModel, onNavigate = { currentRoute = it })
                                "sales" -> SalesScreen(viewModel = viewModel)
                                "products" -> ProductsScreen(viewModel = viewModel)
                                "inventory" -> InventoryScreen(viewModel = viewModel)
                                "requested_items" -> RequestedItemsScreen(viewModel = viewModel)
                                "chatbot" -> ChatbotScreen(viewModel = viewModel)
                                "analytics" -> AnalyticsScreen(viewModel = viewModel)
                                "settings" -> SettingsScreen(viewModel = viewModel)
                                else -> DashboardScreen(viewModel = viewModel, onNavigate = { currentRoute = it })
                            }
                        }
                    }
                }
            }
        }
    }
}

