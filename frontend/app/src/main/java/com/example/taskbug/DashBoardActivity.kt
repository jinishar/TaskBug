package com.example.taskbug

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import android.os.Bundle
import com.example.taskbug.navigation.AppNavGraph
import com.example.taskbug.ui.auth.AuthViewModel

class DashboardActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AppNavGraph(authViewModel = authViewModel)
            }
        }
    }
}
