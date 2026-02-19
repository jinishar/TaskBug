# Firebase Realtime Database - Quick Reference Card

## 🚀 Quick Start (Do This First!)

### 1. Update Firebase Security Rules
- [ ] Go to firebase.google.com
- [ ] Select project: **taskbugcu**
- [ ] Click **Realtime Database** → **Rules** tab
- [ ] Copy content from `firebase_rules.json`
- [ ] Paste and click **Publish**

### 2. Test the Implementation
- [ ] Run app in Android Studio
- [ ] Create a test account
- [ ] Check Logcat for "AuthViewModel" logs
- [ ] Open Firebase Console → Realtime Database → Data tab
- [ ] Verify your user appears in "users" node

---

## 🔧 Code Usage Examples

### In Your UI (e.g., LoginScreen.kt):
```kotlin
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taskbug.ui.auth.AuthViewModel

@Composable
fun LoginScreen() {
    val authViewModel: AuthViewModel = viewModel()
    val isLoading by authViewModel.isLoading.collectAsState(initial = false)
    val authError by authViewModel.authError.collectAsState(initial = null)
    
    // Sign up
    Button(onClick = {
        authViewModel.signUp("John Doe", "john@example.com", "1234567890", "password123")
    }) {
        Text("Sign Up")
    }
    
    // Sign in
    Button(onClick = {
        authViewModel.signIn("john@example.com", "password123")
    }) {
        Text("Sign In")
    }
    
    // Show error if any
    authError?.let {
        Text("Error: $it", color = Color.Red)
    }
}
```

### Access User Profile:
```kotlin
val userProfile by authViewModel.userProfile.collectAsState(initial = null)

userProfile?.let { profile ->
    Text("Welcome ${profile.name}")
    Text("Email: ${profile.email}")
    Text("Location: ${profile.location}")
}
```

### Update Location:
```kotlin
authViewModel.updateUserLocation("New York, NY")
```

### Sign Out:
```kotlin
authViewModel.signOut()
```

---

## 📊 Database Structure

Your database will look like this:

```
taskbugcu (Your Database)
└── users/
    └── [user_uid_1]/
        ├── name: "John Doe"
        ├── email: "john@example.com"
        ├── phone: "1234567890"
        ├── location: "New York, NY"
        └── uid: "[user_uid_1]"
    └── [user_uid_2]/
        ├── name: "Jane Smith"
        ├── email: "jane@example.com"
        ├── phone: "9876543210"
        ├── location: "Los Angeles, CA"
        └── uid: "[user_uid_2]"
```

---

## 🐛 Debugging Checklist

- [ ] INTERNET permission in AndroidManifest.xml
- [ ] firebase-database dependency in build.gradle.kts
- [ ] Database URL matches: `https://taskbugcu-default-rtdb.asia-southeast1.firebasedatabase.app`
- [ ] Security rules published in Firebase Console
- [ ] google-services.json in app folder
- [ ] No "Permission denied" errors in Logcat
- [ ] Data appears in Firebase Console after sign up

---

## 📝 Logcat Filter Tags

To debug, filter Logcat by these tags:
- `AuthViewModel` - All authentication operations
- `Firebase` - General Firebase logs
- `FirebaseDatabase` - Realtime Database specific

---

## 🔐 Security Notes

- Each user can only read/write their own data
- `uid` field must match the parent key (user's ID)
- Required fields: name, email, phone, location, uid
- All validations are checked by Firebase rules

---

## 📱 Testing Scenarios

### Scenario 1: New User Sign Up
```
1. Open app
2. Enter: name="Alice", email="alice@test.com", phone="5555555555", password="Test123!"
3. Check Logcat for "User profile saved successfully"
4. Check Firebase Console for user data
5. Result: ✅ Success
```

### Scenario 2: Returning User Sign In
```
1. Sign out (if logged in)
2. Enter: email="alice@test.com", password="Test123!"
3. Check Logcat for "Profile loaded successfully"
4. Verify profile displays correctly
5. Result: ✅ Success
```

### Scenario 3: Update Location
```
1. After sign in, update location
2. Check Logcat for "Location updated successfully"
3. Check Firebase Console for updated location
4. Result: ✅ Success
```

### Scenario 4: Offline Mode
```
1. Turn on airplane mode
2. Perform location update
3. Data is cached locally
4. Turn off airplane mode
5. Data syncs to Firebase
6. Result: ✅ Success
```

---

## ⚡ Performance Tips

- Offline persistence is enabled (setPersistenceEnabled = true)
- Data is cached locally for faster access
- Operations use `.await()` for proper coroutine handling
- Error handling prevents app crashes

---

## 🆘 If Something Goes Wrong

### Step 1: Check Logcat
```
Filter: "AuthViewModel"
Look for: Error messages, stack traces
```

### Step 2: Common Fixes
- [ ] Restart the app
- [ ] Clear app cache (Settings → Apps → TaskBug → Storage → Clear Cache)
- [ ] Update Firebase rules (most common issue!)
- [ ] Check internet connection
- [ ] Verify google-services.json is present

### Step 3: Verify Firebase Console
1. Go to firebase.google.com
2. Select "taskbugcu" project
3. Realtime Database → Data tab
4. Look for "users" node
5. Check if your test data exists

---

## 📚 Key Files

- `AuthViewModel.kt` - Main authentication logic (✅ Updated)
- `build.gradle.kts` - Dependencies (✅ Updated)
- `AndroidManifest.xml` - Permissions (✅ Updated)
- `firebase_rules.json` - Security rules (Copy to Firebase Console)

---

## 🎓 Learning Resources

- Firebase Docs: https://firebase.google.com/docs/database
- Kotlin Coroutines: https://kotlinlang.org/docs/coroutines-overview.html
- Android Architecture: https://developer.android.com/jetpack/guide

---

## ✅ Status

- [x] Dependencies added
- [x] Permissions configured
- [x] Code enhanced with error handling
- [x] Logging added for debugging
- [x] Offline persistence enabled
- [ ] Security rules updated (DO THIS!)
- [ ] Tested and working (DO THIS!)

**You're ready to go! 🚀**

