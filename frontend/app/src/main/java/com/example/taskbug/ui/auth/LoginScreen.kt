package com.example.taskbug.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val AppTeal = Color(0xFF0F766E)
private val AppBackground = Color(0xFFF9FAFB)
private val TextPrimary = Color(0xFF111827)
private val TextSecondary = Color(0xFF6B7280)

@Composable
fun LoginScreen(
    onLoginClicked: (String, String) -> Unit,
    onSignUpClicked: (String, String, String, String) -> Unit,
    authError: String? = null,
    isLoading: Boolean = false
) {
    var isSignUpMode by remember { mutableStateOf(false) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))

            // Logo area
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(AppTeal, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("🐛", fontSize = 36.sp)
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = if (isSignUpMode) "Create Account" else "Welcome back",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = if (isSignUpMode) "Join TaskBug to post or complete tasks" else "Sign in to your TaskBug account",
                fontSize = 15.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(32.dp))

            // Name + Phone (sign up only)
            if (isSignUpMode) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AppTeal)
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AppTeal)
                )
                Spacer(Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AppTeal)
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AppTeal),
                trailingIcon = {
                    val image = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(imageVector = image, contentDescription = null)
                    }
                }
            )

            // Error
            if (authError != null) {
                Spacer(Modifier.height(8.dp))
                Text(authError, color = MaterialTheme.colorScheme.error, fontSize = 13.sp, textAlign = TextAlign.Center)
            }

            Spacer(Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator(color = AppTeal)
            } else {
                Button(
                    onClick = {
                        if (isSignUpMode) {
                            onSignUpClicked(name, email, phone, password)
                        } else {
                            onLoginClicked(email, password)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppTeal)
                ) {
                    Text(
                        if (isSignUpMode) "Create Account" else "Login",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Toggle between login / sign up
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isSignUpMode) "Already have an account? " else "Don't have an account? ",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
                Text(
                    text = if (isSignUpMode) "Login" else "Sign Up",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTeal,
                    modifier = Modifier.clickable {
                        isSignUpMode = !isSignUpMode
                        email = ""; password = ""; name = ""; phone = ""
                    }
                )
            }

            Spacer(Modifier.height(48.dp))
        }
    }
}