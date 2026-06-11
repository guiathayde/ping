package com.guiathayde.ping

import android.net.Uri
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
import com.guiathayde.ping.ui.chat.ChatScreen
import com.guiathayde.ping.ui.conversations.ConversationsScreen
import com.guiathayde.ping.ui.search.SearchScreen
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
                                onLoginSuccess = {
                                    navController.navigate("Conversations") {
                                        popUpTo("Auth") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("Conversations") {
                            ConversationsScreen(
                                onConversationClick = { conversationId, name, username ->
                                    navController.navigate(
                                        "Chat/$conversationId/${Uri.encode(name)}/${Uri.encode(username)}"
                                    )
                                },
                                onSearchClick = { navController.navigate("Search") },
                                onLogout = {
                                    navController.navigate("Auth") {
                                        popUpTo("Conversations") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("Search") {
                            SearchScreen(
                                onBack = { navController.popBackStack() },
                                onConversationCreated = { navController.popBackStack() }
                            )
                        }
                        composable("Chat/{conversationId}/{participantName}/{participantUsername}") { backStackEntry ->
                            ChatScreen(
                                conversationId = backStackEntry.arguments
                                    ?.getString("conversationId") ?: "",
                                participantName = backStackEntry.arguments
                                    ?.getString("participantName") ?: "",
                                participantUsername = backStackEntry.arguments
                                    ?.getString("participantUsername") ?: "",
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
