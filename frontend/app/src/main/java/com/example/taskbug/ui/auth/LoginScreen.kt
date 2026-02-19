package com.example.taskbug.ui.auth

import androidx.compose.animation.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─── Design Tokens ───────────────────────────────────────────────────────────
private val Cream      = Color(0xFFFAF6F1)  // page background
private val White      = Color(0xFFFFFFFF)
private val Terra     = Color(0xFFC1603A)  // terracotta accent
private val TerraLt   = Color(0xFFF5EDE8)  // light terracotta surface
private val Ink        = Color(0xFF1E1712)  // near-black for text & CTA
private val Slate      = Color(0xFF5C4A3A)  // secondary text
private val Muted      = Color(0xFFA08878)  // placeholder / hint
private val Border     = Color(0xFFEAE0D8)  // input borders
private val ErrorRed   = Color(0xFFC0392B)

// ─── Typography ──────────────────────────────────────────────────────────────
// Add DM Serif Display & DM Sans to your res/font folder and update accordingly.
// Fallback: use default serif / sans if not yet added.
private val DisplayFont = FontFamily.Serif   // replace with DM Serif Display
private val BodyFont    = FontFamily.Default // replace with DM Sans

// ─── Screen ──────────────────────────────────────────────────────────────────
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LoginScreen(
    onLoginClicked: (String, String) -> Unit,
    onSignUpClicked: (String, String, String, String) -> Unit,
    authError: String? = null,
    isLoading: Boolean = false
) {
    var isSignUpMode by remember { mutableStateOf(false) }
    var email       by remember { mutableStateOf("") }
    var password    by remember { mutableStateOf("") }
    var name        by remember { mutableStateOf("") }
    var phone       by remember { mutableStateOf("") }
    var pwVisible   by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(Modifier.height(64.dp))

            // ── Tab Row ──────────────────────────────────────────────────────
            TabSwitcher(
                isSignUp = isSignUpMode,
                onToggle = {
                    isSignUpMode = it
                    email = ""; password = ""; name = ""; phone = ""
                }
            )

            Spacer(Modifier.height(32.dp))

            // ── Branding ─────────────────────────────────────────────────────
            LogoMark()
            Spacer(Modifier.height(20.dp))

            // Terra accent line
            Box(
                modifier = Modifier
                    .width(32.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Terra)
            )
            Spacer(Modifier.height(10.dp))

            AnimatedContent(targetState = isSignUpMode, label = "headline") { signup ->
                Column {
                    Text(
                        text = buildAnnotatedString {
                            if (signup) {
                                append("Join the\n")
                                withStyle(SpanStyle(color = Terra, fontStyle = FontStyle.Italic)) {
                                    append("community")
                                }
                            } else {
                                append("Welcome\nto ")
                                withStyle(SpanStyle(color = Terra, fontStyle = FontStyle.Italic)) {
                                    append("TaskBug")
                                }
                            }
                        },
                        fontSize = 32.sp,
                        fontFamily = DisplayFont,
                        fontWeight = FontWeight.Normal,
                        color = Ink,
                        lineHeight = 38.sp,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = if (signup) "Post tasks. Earn. Help nearby." else "Tasks nearby. Done quickly.",
                        fontSize = 15.sp,
                        color = Muted,
                        fontFamily = BodyFont,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            Spacer(Modifier.height(36.dp))

            // ── Fields ───────────────────────────────────────────────────────
            AnimatedVisibility(
                visible = isSignUpMode,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TbField(
                            value = name,
                            onValueChange = { name = it },
                            label = "Full Name",
                            placeholder = "Arjun Singh",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    TbField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = "Phone",
                        placeholder = "+91 98765 43210",
                        keyboardType = KeyboardType.Phone
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }

            TbField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                placeholder = "you@example.com",
                keyboardType = KeyboardType.Email
            )

            Spacer(Modifier.height(16.dp))

            TbField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                placeholder = if (isSignUpMode) "Min. 8 characters" else "••••••••",
                keyboardType = KeyboardType.Password,
                visualTransformation = if (pwVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { pwVisible = !pwVisible }) {
                        Icon(
                            imageVector = if (pwVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = Muted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )

            // Forgot password (login only)
            AnimatedVisibility(visible = !isSignUpMode) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        "Forgot password?",
                        fontSize = 13.sp,
                        color = Terra,
                        fontWeight = FontWeight.W500,
                        modifier = Modifier.clickable { }
                    )
                }
            }

            // Auth error
            if (authError != null) {
                Spacer(Modifier.height(10.dp))
                Text(
                    authError,
                    fontSize = 13.sp,
                    color = ErrorRed,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(28.dp))

            // ── Primary CTA ──────────────────────────────────────────────────
            if (isLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Terra, modifier = Modifier.size(28.dp))
                }
            } else {
                Button(
                    onClick = {
                        if (isSignUpMode) onSignUpClicked(name, email, phone, password)
                        else onLoginClicked(email, password)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = White),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = if (isSignUpMode) "Create Account  →" else "Continue  →",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.2.sp
                    )
                }
            }

            // Google button (login only)
            AnimatedVisibility(visible = !isSignUpMode) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    OrDivider()
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { /* Google auth */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Border),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink)
                    ) {
                        Text("G", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4285F4))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Continue with Google",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.W500,
                            color = Ink
                        )
                    }
                }
            }

            // Terms (sign up only)
            AnimatedVisibility(visible = isSignUpMode) {
                Spacer(Modifier.height(16.dp))
            }
            AnimatedVisibility(visible = isSignUpMode) {
                Text(
                    "By signing up you agree to our Terms and Privacy Policy",
                    fontSize = 12.sp,
                    color = Muted,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(28.dp))

            // ── Toggle ───────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isSignUpMode) "Already have an account?" else "New to TaskBug?",
                    fontSize = 14.sp,
                    color = Muted
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = if (isSignUpMode) "Login" else "Create an account",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Terra,
                    modifier = Modifier.clickable { isSignUpMode = !isSignUpMode }
                )
            }

            Spacer(Modifier.height(48.dp))
        }
    }
}

// ─── Components ──────────────────────────────────────────────────────────────

@Composable
private fun LogoMark() {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Ink),
        contentAlignment = Alignment.Center
    ) {
        Text("🐛", fontSize = 26.sp)
    }
}

@Composable
private fun TabSwitcher(isSignUp: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFEDE5DB))
            .padding(4.dp)
    ) {
        listOf("Login" to false, "Sign Up" to true).forEach { (label, mode) ->
            val selected = isSignUp == mode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (selected) White else Color.Transparent)
                    .clickable { onToggle(mode) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    fontSize = 14.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (selected) Ink else Muted
                )
            }
        }
    }
}

@Composable
private fun TbField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            label.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Slate,
            letterSpacing = 0.08.sp
        )
        Spacer(Modifier.height(7.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            placeholder = {
                Text(placeholder, fontSize = 15.sp, color = Muted)
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            visualTransformation = visualTransformation,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            trailingIcon = trailingIcon,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Terra,
                unfocusedBorderColor = Border,
                focusedContainerColor = White,
                unfocusedContainerColor = White,
                cursorColor = Terra
            ),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 15.sp,
                color = Ink,
                fontFamily = BodyFont
            )
        )
    }
}

@Composable
private fun OrDivider() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = Border)
        Text(
            "  or  ",
            fontSize = 12.sp,
            color = Muted,
            fontWeight = FontWeight.W500,
            letterSpacing = 0.05.sp
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = Border)
    }
}