package com.example.myapplication

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun MainScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "WELCOME",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(200.dp))

        Text(
            text = "Please choose a method to predict your mental health:",
            fontSize = 25.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(80.dp))

        Button(
            onClick = { navController.navigate("text_screen") },
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAFEB)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Text", color = Color.White, fontSize = 25.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate("voice_screen") },
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAFEB)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Voice", color = Color.White, fontSize = 25.sp)
        }
    }
}
