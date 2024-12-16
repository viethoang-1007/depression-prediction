package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.File
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.MultipartBody
import kotlinx.coroutines.withContext
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject



@Composable
fun VoiceScreen(navController: NavController) {

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
    var isRecording by remember { mutableStateOf(false) }
    var recorder: MediaRecorder? by remember { mutableStateOf(null) }
    val context = LocalContext.current
    var audioFilePath: String? by remember { mutableStateOf(null) }

    // List to store predictions
    val predictions = remember { mutableStateListOf<Float>() }

    fun startRecording() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Microphone permission required", Toast.LENGTH_SHORT).show()
            return
        }

        val tempFile = File(context.cacheDir, "temp_audio.mp4")
        audioFilePath = tempFile.absolutePath
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(tempFile.absolutePath)

            try {
                prepare()
                start()
                isRecording = true
                Toast.makeText(context, "Recording started", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(context, "Recording failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun sendAudioToServer(audioFile: File, onResult: (Float) -> Unit) {
        val client = OkHttpClient()

        val requestBody = audioFile.asRequestBody("audio/mp4".toMediaTypeOrNull())
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", audioFile.name, requestBody)
            .build()

        val request = Request.Builder()
            .url("http://192.168.0.9:5000/upload") // Đảm bảo URL đúng
            .post(body)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    // Assuming server returns a float prediction in response body
                    val responseString = response.body?.string() ?: ""
                    Log.d("Response", "Response body: $responseString")
                    val gson = Gson()
                    val responseJson = gson.fromJson(responseString, JsonObject::class.java)
                    val prediction = responseJson.get("prediction")?.asFloat ?: 0f
                    Log.d("Prediction", "Prediction received: $prediction")


                    withContext(Dispatchers.Main) {
                        // Save the prediction to the list
                        predictions.add(prediction)
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

    fun stopRecording() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
        isRecording = false
        Toast.makeText(context, "Recording stopped", Toast.LENGTH_SHORT).show()

        // Send audio to the server for prediction
        val audioFile = File(audioFilePath ?: "")
        if (audioFile.exists()) {
            sendAudioToServer(audioFile) { prediction ->
                predictions.add(prediction)
            }
        } else {
            Toast.makeText(context, "Audio file does not exist", Toast.LENGTH_SHORT).show()
        }
    }

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

        Button(
            onClick = {
                if (isRecording) {
                    stopRecording()
                } else {
                    startRecording()
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAFEB)),
            shape = RoundedCornerShape(25.dp)
        ) {
            Text(text = if (isRecording) "Stop Recording" else "Start Recording")
        }

        Spacer(modifier = Modifier.height(50.dp))

        Button(
            onClick = {
                if (currentQuestionIndex < questions.size - 1) {
                    currentQuestionIndex++
                } else {
                    // Calculate the result
                    val result = calculateResult()

                    // Send the result to ResultScreen
                    navController.navigate("result_screen/${result}")

                    // Clear predictions list
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