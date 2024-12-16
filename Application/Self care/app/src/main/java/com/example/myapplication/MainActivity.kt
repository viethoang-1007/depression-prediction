package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "main_screen") {
                    composable("main_screen") { MainScreen(navController) }
                    composable("text_screen") { TextScreen(navController) }
                    composable("voice_screen") { VoiceScreen(navController) }
                    composable("result_screen/{resultMessage}") { backStackEntry ->
                        val resultMessage = backStackEntry.arguments?.getString("resultMessage")
                        ResultScreen(navController = navController, resultMessage = resultMessage ?: "No result")
                    }
                }
            }
        }
    }
}
