package com.example.taskbug.ui.auth

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

// Data class to hold user profile information
data class UserProfile(
    var name: String = "",
    var email: String = "",
    var phone: String = "",
    var location: String = ""
)

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    // Correctly initialize FirebaseDatabase to resolve build error
    private val database = FirebaseDatabase.getInstance("https://taskbugcu-default-rtdb.asia-southeast1.firebasedatabase.app")
    private val usersRef = database.getReference("users")

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
        if (auth.currentUser != null) {
            fetchCurrentUserProfile()
        }
    }

    fun signUp(name: String, email: String, phone: String, pass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
                val user = authResult.user
                if (user != null) {
                    // Save user details to Realtime Database
                    val userProfile = UserProfile(name = name, email = email, phone = phone, location = "")
                    usersRef.child(user.uid).setValue(userProfile).await()
                    _signUpSuccess.value = true
                }
            } catch (e: Exception) {
                _authError.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetSignUpSuccess() {
        _signUpSuccess.value = false
    }

    fun signIn(email: String, pass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            try {
                auth.signInWithEmailAndPassword(email, pass).await()
                fetchCurrentUserProfile()
                _userLoggedIn.value = true
            } catch (e: Exception) {
                _authError.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun fetchCurrentUserProfile() {
        val user = auth.currentUser
        if (user != null) {
            viewModelScope.launch {
                _isLoading.value = true
                _authError.value = null
                try {
                    val snapshot = usersRef.child(user.uid).get().await()
                    if (snapshot.exists()) {
                        _userProfile.value = snapshot.getValue(UserProfile::class.java)
                        if (_userProfile.value == null) {
                            _authError.value = "Failed to parse profile data."
                        }
                    } else {
                        _authError.value = "Profile data does not exist."
                    }
                } catch (e: Exception) {
                    _authError.value = "Failed to load profile: ${e.message}"
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    fun updateUserProfile(name: String, phone: String) {
        val user = auth.currentUser
        if (user != null) {
            viewModelScope.launch {
                _isLoading.value = true
                _authError.value = null
                try {
                    val updates = mapOf<String, Any>(
                        "name" to name,
                        "phone" to phone
                    )
                    usersRef.child(user.uid).updateChildren(updates).await()
                    val currentProfile = _userProfile.value
                    if (currentProfile != null) {
                        _userProfile.value = currentProfile.copy(name = name, phone = phone)
                    }
                } catch (e: Exception) {
                    _authError.value = "Failed to update profile: ${e.message}"
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    fun updateUserLocation(location: String) {
        val user = auth.currentUser
        if (user != null) {
            viewModelScope.launch {
                try {
                    usersRef.child(user.uid).child("location").setValue(location).await()
                    _userProfile.value = _userProfile.value?.copy(location = location)
                } catch (e: Exception) {
                    _authError.value = "Failed to update location: ${e.message}"
                }
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _userLoggedIn.value = false
        _userProfile.value = null
    }
}
