package com.example.taskbug.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "AuthViewModel"

// Data class to hold user profile information
data class UserProfile(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val location: String = "",
    val uid: String = ""
)

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth

    // Initialize FirebaseDatabase - using the URL from your Firebase project
    private val database: FirebaseDatabase by lazy {
        try {
            FirebaseDatabase.getInstance("https://taskbugcu-default-rtdb.asia-southeast1.firebasedatabase.app").apply {
                setPersistenceEnabled(true) // Enable offline persistence
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firebase Database: ${e.message}", e)
            FirebaseDatabase.getInstance() // Fallback to default instance
        }
    }

    private val usersRef by lazy { database.getReference("users") }

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError = _authError.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile = _userProfile.asStateFlow()

    private val _signUpSuccess = MutableStateFlow(false)
    val signUpSuccess = _signUpSuccess.asStateFlow()

    // Check if user is logged in at startup
    private val _userLoggedIn = MutableStateFlow(auth.currentUser != null)
    val userLoggedIn = _userLoggedIn.asStateFlow()

    init {
        Log.d(TAG, "AuthViewModel initialized, current user: ${auth.currentUser?.uid}")
        if (auth.currentUser != null) {
            fetchCurrentUserProfile()
        }
    }

    fun signUp(name: String, email: String, phone: String, pass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            try {
                Log.d(TAG, "Starting sign up for email: $email")
                val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
                val user = authResult.user
                if (user != null) {
                    // Save user details to Realtime Database
                    val userProfile = UserProfile(
                        name = name,
                        email = email,
                        phone = phone,
                        location = "",
                        uid = user.uid
                    )
                    Log.d(TAG, "User created: ${user.uid}, saving to database...")

                    // Save to database
                    usersRef.child(user.uid).setValue(userProfile).await()
                    Log.d(TAG, "User profile saved successfully to database")

                    _userProfile.value = userProfile
                    _userLoggedIn.value = false // Keep false to show login screen
                    _signUpSuccess.value = true  // Show success popup
                } else {
                    _authError.value = "Failed to create user account"
                    Log.e(TAG, "User creation returned null")
                }
            } catch (e: Exception) {
                val errorMsg = "Sign up error: ${e.message}"
                _authError.value = errorMsg
                Log.e(TAG, errorMsg, e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signIn(email: String, pass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            try {
                Log.d(TAG, "Starting sign in for email: $email")
                auth.signInWithEmailAndPassword(email, pass).await()
                Log.d(TAG, "Sign in successful, fetching profile...")

                // Give database a moment to respond
                fetchCurrentUserProfile()

                _userLoggedIn.value = true
                Log.d(TAG, "User logged in successfully")
            } catch (e: Exception) {
                val errorMsg = "Sign in error: ${e.message}"
                _authError.value = errorMsg
                Log.e(TAG, errorMsg, e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Public method to refresh user profile from database
     */
    fun refreshUserProfile() {
        Log.d(TAG, "Manual profile refresh requested")
        fetchCurrentUserProfile()
    }

    private fun fetchCurrentUserProfile() {
        val user = auth.currentUser
        if (user != null) {
            viewModelScope.launch {
                try {
                    Log.d(TAG, "Fetching profile for user: ${user.uid}")
                    val snapshot = usersRef.child(user.uid).get().await()
                    Log.d(TAG, "Database snapshot received, exists: ${snapshot.exists()}")

                    val profile = snapshot.getValue(UserProfile::class.java)
                    if (profile != null) {
                        Log.d(TAG, "Profile loaded successfully: ${profile.name} (${profile.email})")
                        _userProfile.value = profile
                    } else {
                        Log.w(TAG, "User profile is null in database for uid: ${user.uid}")
                        Log.w(TAG, "Snapshot value: ${snapshot.value}")
                        _authError.value = "Profile data not found in database"

                        // If profile doesn't exist in database but user is authenticated,
                        // this shouldn't happen, but try to rebuild it
                        if (user.email != null) {
                            Log.d(TAG, "Attempting to rebuild user profile from auth data")
                            val fallbackProfile = UserProfile(
                                name = user.displayName ?: "User",
                                email = user.email ?: "",
                                phone = "",
                                location = "",
                                uid = user.uid
                            )
                            _userProfile.value = fallbackProfile
                        }
                    }
                } catch (e: Exception) {
                    val errorMsg = "Failed to load profile: ${e.message}"
                    _authError.value = errorMsg
                    Log.e(TAG, errorMsg, e)
                    e.printStackTrace()
                }
            }
        } else {
            Log.w(TAG, "Current user is null")
        }
    }

    fun updateUserLocation(location: String) {
        val user = auth.currentUser
        if (user != null) {
            viewModelScope.launch {
                try {
                    Log.d(TAG, "Updating location for user: ${user.uid}")
                    usersRef.child(user.uid).child("location").setValue(location).await()
                    _userProfile.value = _userProfile.value?.copy(location = location)
                    Log.d(TAG, "Location updated successfully to: $location")
                } catch (e: Exception) {
                    val errorMsg = "Failed to update location: ${e.message}"
                    _authError.value = errorMsg
                    Log.e(TAG, errorMsg, e)
                }
            }
        } else {
            Log.w(TAG, "Cannot update location: no user logged in")
        }
    }

    fun signOut() {
        auth.signOut()
        _userLoggedIn.value = false
        _userProfile.value = null
    }

    fun resetSignUpSuccess() {
        _signUpSuccess.value = false
    }
}
