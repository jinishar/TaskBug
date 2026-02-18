package com.example.taskbug.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Data class to hold user profile information
data class UserProfile(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val location: String = ""
)

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    private val firestore = Firebase.firestore

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError = _authError.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile = _userProfile.asStateFlow()

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
                    // Save user details to Firestore, with an empty location to start
                    val userProfile = UserProfile(name = name, email = email, phone = phone, location = "")
                    firestore.collection("users").document(user.uid).set(userProfile).await()
                    _userProfile.value = userProfile
                    _userLoggedIn.value = true
                }
            } catch (e: Exception) {
                _authError.value = e.message
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
                try {
                    val document = firestore.collection("users").document(user.uid).get().await()
                    _userProfile.value = document.toObject<UserProfile>()
                } catch (e: Exception) {
                    _authError.value = "Failed to load profile: ${e.message}"
                }
            }
        }
    }

    fun updateUserLocation(location: String) {
        val user = auth.currentUser
        if (user != null) {
            viewModelScope.launch {
                try {
                    firestore.collection("users").document(user.uid).update("location", location).await()
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
