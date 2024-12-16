package com.example.myapplication


import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.IOException
import kotlinx.coroutines.withContext
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject

@Composable
fun TextScreen(navController: NavController) {
    val questions = listOf(
        "How are you today?",
        "Where are you from?",
        "Do you like the place where you’re living?",
        "What do you like about the place where you live?",
        "Why do you like them?",
        "What do you dislike about the place where you live?",
        "Why do you dislike them?",
        "How often do you go back to your hometown?",
        "How many people are in your family?",
        "How close are you to your family ?",
        "Can you share more about the problems in your family, such as the relationships between members, issues in taking care of your parents or children, financial problems, health problems, etc.?",
        "What are your hobbies?",
        "Could you tell me more about your hobby? For example, how often you do it, whether you do it alone or with somebody...",
        "What is your job?",
        "Do you love it?",
        "How about your colleagues?",
        "How do you feel about your work? Does it usually make you stressed? And why do you feel that way?",
        "Can you share more about your work? For example, the problems with your clients, your boss, etc.",
        "Do you consider yourself an introvert?",
        "What do you do to relax?",
        "What are some things that make you really mad?",
        "What do you do when you're annoyed?",
        "When was the last time you argued with someone and what was it about?",
        "Who's someone that's been a positive influence in your life?",
        "Can you tell me more about that?",
        "What's one of your most memorable experiences?",
        "Is there anything you regret?",
        "Could you have done anything to avoid it?",
        "What advice would you give yourself ten or twenty years ago?",
        "How would your best friend describe you?",
        "Could you tell me about something you did recently that you really enjoyed?"
    )

    var currentQuestionIndex by remember { mutableStateOf(0) }
    var answer by remember { mutableStateOf("") }
    val userAnswers = remember { mutableStateListOf<String>() }
    val predictions = remember { mutableStateListOf<Float>() }

    // Lấy context để hiển thị Toast
    val context = LocalContext.current

    // Hàm gửi câu trả lời lên máy chủ
    fun sendAnswerToServer(answer: String, context: Context, onResult: (Float) -> Unit) {
        val client = OkHttpClient()

        val body = FormBody.Builder()
            .add("text", answer)
            .build()

        val request = Request.Builder()
            .url("http://192.168.0.9:5000/upload") // Đảm bảo URL đúng
            .post(body)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseString = response.body?.string() ?: ""
                    Log.d("Response", "Response body: $responseString")
                    val gson = Gson()
                    val responseJson = gson.fromJson(responseString, JsonObject::class.java)
                    val prediction = responseJson.get("prediction")?.asFloat ?: 0f
                    Log.d("Prediction", "Prediction received: $prediction")

                    withContext(Dispatchers.Main) {
                        predictions.add(prediction)
                        onResult(prediction)

                        // Hiển thị Toast cho kết quả dự đoán
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Upload Failed with code: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Upload Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Hàm tính kết quả dựa trên các dự đoán
    fun calculateResult(): String {
        if (predictions.isEmpty()) return "No predictions available."

        val averagePrediction = predictions.average()
        return if (averagePrediction > 0.5) {
            "You may be showing signs of depression. You should seek help from a mental health professional.\nLIFE IS BEAUTIFUL!"
        } else {
            "Your mental health is good. Let's maintain a healthy lifestyle and enjoy life\nLIFE IS BEAUTIFUL!"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = questions[currentQuestionIndex],
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(50.dp))

        TextField(
            value = answer,
            onValueChange = { answer = it },
            placeholder = { Text("Your answer") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                // Gửi câu trả lời lên máy chủ khi nhấn "Send"
                sendAnswerToServer(answer, context) { prediction ->
                    // Sau khi gửi câu trả lời và nhận dự đoán, thêm vào danh sách và làm trống câu trả lời
                    userAnswers.add(answer)
                    answer = ""
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAFEB)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Send", color = Color.White, fontSize = 25.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (currentQuestionIndex < questions.size - 1) {
                    currentQuestionIndex++
                } else {
                    // Tính kết quả và chuyển đến ResultScreen
                    val result = calculateResult()
                    navController.navigate("result_screen/$result")

                    // Xóa danh sách câu trả lời và dự đoán
                    userAnswers.clear()
                    predictions.clear()
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAFEB)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (currentQuestionIndex < questions.size - 1) "Next" else "Get Result",
                color = Color.White,
                fontSize = 25.sp
            )
        }
    }
}