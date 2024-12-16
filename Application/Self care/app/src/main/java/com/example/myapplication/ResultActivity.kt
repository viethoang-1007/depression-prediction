package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color

@Composable
fun ResultScreen(navController: NavController, resultMessage: String?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = resultMessage ?: "No result",
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(50.dp))

        Button(
            onClick = {
                // Điều hướng trực tiếp về MainScreen và xóa các màn hình trước đó
                navController.navigate("main_screen") {
                    popUpTo("main_screen") { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAFEB)),
            shape = RoundedCornerShape(25.dp)
        ) {
            Text(text = "Back to Main Screen")
        }
    }
}