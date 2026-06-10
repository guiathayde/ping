package com.guiathayde.ping

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.guiathayde.ping.ui.auth.AuthScreen
import com.guiathayde.ping.ui.conversations.ConversationsScreen
import com.guiathayde.ping.ui.theme.PingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val container = (application as PingApplication).container
                    val startDestination =
                        if (container.authRepository.isLoggedIn) "Conversations" else "Auth"

                    NavHost(
                        navController = navController,
                        startDestination = startDestination
                    ) {
                        composable("Auth") {
                            AuthScreen(
                                onLoginSuccess = { navController.navigate("Conversations") }
                            )
                        }
                        composable("Conversations") {
                            ConversationsScreen()
                        }
                    }
                }
            }
        }
    }
}
