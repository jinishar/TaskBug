package com.example.taskbug

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskbug.navigation.AppNavGraph
import com.example.taskbug.ui.auth.AuthViewModel
import kotlinx.coroutines.delay

/* ---------- COLORS ---------- */

private val Primary = Color(0xFF0F766E)
private val Background = Color(0xFFF9FAFB)

/* ---------- MAIN ACTIVITY ---------- */

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                val userLoggedIn by authViewModel.userLoggedIn.collectAsState()
                val signUpSuccess by authViewModel.signUpSuccess.collectAsState()
                val authError by authViewModel.authError.collectAsState()
                
                val context = androidx.compose.ui.platform.LocalContext.current
                var wasLoggedIn by remember { mutableStateOf(userLoggedIn) }

                LaunchedEffect(userLoggedIn) {
                    if (userLoggedIn && !wasLoggedIn) {
                        android.widget.Toast.makeText(context, "Login Successful!", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    wasLoggedIn = userLoggedIn
                }

                LaunchedEffect(authError) {
                    authError?.let { errorMsg ->
                        android.widget.Toast.makeText(context, errorMsg, android.widget.Toast.LENGTH_LONG).show()
                    }
                }

                if (userLoggedIn) {
                    AppNavGraph(authViewModel = authViewModel) // If user is logged in, show the main app
                } else {
                    // Otherwise, show the original login/signup flow
                    var screen by remember { mutableStateOf("splash") }
                    var showSuccessDialog by remember { mutableStateOf(false) }

                    // Listen to signUpSuccess and show dialog
                    LaunchedEffect(signUpSuccess) {
                        if (signUpSuccess) {
                            showSuccessDialog = true
                        }
                    }

                    // Success Dialog
                    if (showSuccessDialog) {
                        SuccessRegistrationDialog(
                            onDismiss = {
                                showSuccessDialog = false
                                authViewModel.resetSignUpSuccess()
                                screen = "login" // Route back to login
                            }
                        )
                    }

                    when (screen) {
                        "splash" -> SplashScreen { screen = "login" }

                        "login" -> LoginScreen(
                            onLogin = { email, password -> authViewModel.signIn(email, password) },
                            onSignup = { screen = "signup" },
                            onForgot = { screen = "forgot" }
                        )

                        "signup" -> SignupScreen(
                            onBack = { screen = "login" },
                            onDone = { name, email, phone, password -> authViewModel.signUp(name, email, phone, password) }
                        )

                        "forgot" -> ForgotPasswordScreen(
                            onBack = { screen = "login" },
                            onNext = { screen = "otp" }
                        )

                        "otp" -> OtpScreen(
                            onBack = { screen = "login" },
                            onDone = { /* This is now handled by the login state */ }
                        )
                    }
                }
            }
        }
    }
}

/* ---------- SPLASH ---------- */

@Composable
fun SplashScreen(onFinish: () -> Unit) {
    val scale = remember { Animatable(0.6f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, tween(1000))
        scale.animateTo(1f, tween(1000))
        delay(1800)
        onFinish()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "TaskBug",
            fontSize = 38.sp,
            fontWeight = FontWeight.Bold,
            color = Primary,
            modifier = Modifier.scale(scale.value).alpha(alpha.value)
        )
    }
}

/* ---------- TOP BAR ---------- */

@Composable
fun BackTopBar(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
    }
}

/* ---------- LOGIN ---------- */

@Composable
fun LoginScreen(
    onLogin: (String, String) -> Unit,
    onSignup: () -> Unit,
    onForgot: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().background(Background).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier.size(180.dp)
        )

        Spacer(Modifier.height(20.dp))
        Text("Welcome Back", fontSize = 26.sp, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(24.dp))
        AppTextField("Email", email) { email = it }
        AppTextField("Password", password, password = true) { password = it }

        TextButton(onClick = onForgot, modifier = Modifier.align(Alignment.End)) {
            Text("Forgot Password?", color = Primary)
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { onLogin(email, password) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Login")
        }

        TextButton(onClick = onSignup) {
            Text("Create an Account", color = Primary)
        }
    }
}

/* ---------- SIGNUP ---------- */

@Composable
fun SignupScreen(onBack: () -> Unit, onDone: (String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().background(Background).padding(24.dp)
    ) {
        BackTopBar("Create Account", onBack)

        Spacer(Modifier.height(32.dp))
        AppTextField("Full Name", name) { name = it }
        AppTextField("Email", email) { email = it }
        AppTextField("Phone Number", phone, KeyboardType.Phone) { phone = it }
        AppTextField("Password", password, password = true) { password = it }

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { onDone(name, email, phone, password) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Sign Up")
        }
    }
}

/* ---------- FORGOT PASSWORD ---------- */

@Composable
fun ForgotPasswordScreen(onBack: () -> Unit, onNext: () -> Unit) {
    var value by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().background(Background).padding(24.dp)
    ) {
        BackTopBar("Forgot Password", onBack)

        Spacer(Modifier.height(32.dp))
        AppTextField("Email or Phone", value) { value = it }

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Send OTP")
        }
    }
}

/* ---------- OTP ---------- */

@Composable
fun OtpScreen(onBack: () -> Unit, onDone: () -> Unit) {
    var otp by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().background(Background).padding(24.dp)
    ) {
        BackTopBar("OTP Verification", onBack)

        Spacer(Modifier.height(32.dp))
        AppTextField("Enter OTP", otp, KeyboardType.Number) {
            if (it.length <= 6) otp = it
        }

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Verify OTP")
        }
    }
}

/* ---------- COMMON TEXT FIELD ---------- */

@Composable
fun AppTextField(
    label: String,
    value: String,
    keyboard: KeyboardType = KeyboardType.Text,
    password: Boolean = false,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        visualTransformation = if (password)
            PasswordVisualTransformation()
        else VisualTransformation.None,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(12.dp))
}

/* ---------- SUCCESS REGISTRATION DIALOG ---------- */

@Composable
fun SuccessRegistrationDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Registration Successful!",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF10B981)
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Success Icon
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_dialog_info),
                    contentDescription = "Success",
                    modifier = Modifier
                        .size(60.dp)
                        .padding(bottom = 16.dp),
                    tint = Color(0xFF10B981)
                )

                Text(
                    "Your account has been created successfully!\n\nPlease log in with your credentials to continue.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Continue to Login", color = Color.White)
            }
        },
        modifier = Modifier
            .padding(16.dp)
            .background(Color.White, shape = RoundedCornerShape(16.dp)),
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

