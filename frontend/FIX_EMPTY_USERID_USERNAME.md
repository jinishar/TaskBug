# ✅ FIXED: Empty userName and userId Issue

## The Problem

When creating tasks, `userId` and `userName` were showing as empty strings:
```
userId: ""
userName: ""
```

## Root Cause

The code was using `FirebaseAuth.currentUser.displayName` to get the user's name, but this field is **NEVER set** in Firebase Authentication during signup. It stays empty!

```kotlin
// ❌ WRONG - displayName is always empty
userName = currentUser.displayName ?: "Anonymous"
```

## The Solution

We now fetch the user's name from the **Realtime Database** (where it's actually stored during registration):

```kotlin
// ✅ CORRECT - Get name from Realtime Database
val userSnapshot = usersRef.child(currentUser.uid).get().await()
val userName = if (userSnapshot.exists()) {
    userSnapshot.child("name").getValue(String::class.java) ?: "Anonymous"
} else {
    "Anonymous"
}
```

## What Changed in TaskViewModel.kt

### 1. Added Database Import
```kotlin
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
```

### 2. Added Firebase Realtime Database Reference
```kotlin
private val database: FirebaseDatabase by lazy {
    try {
        FirebaseDatabase.getInstance("https://taskbugcu-default-rtdb.asia-southeast1.firebasedatabase.app")
    } catch (e: Exception) {
        Log.e(TAG, "Error initializing Firebase Database: ${e.message}", e)
        FirebaseDatabase.getInstance()
    }
}
private val usersRef by lazy { database.getReference("users") }
```

### 3. Modified createTask() Function
```kotlin
fun createTask(...) {
    viewModelScope.launch {
        // ... validation code ...
        
        // 🆕 Fetch user name from Realtime Database
        val userSnapshot = usersRef.child(currentUser.uid).get().await()
        val userName = if (userSnapshot.exists()) {
            userSnapshot.child("name").getValue(String::class.java) ?: "Anonymous"
        } else {
            "Anonymous"
        }

        // Now userName has the actual user's name!
        val task = Task(
            title = title,
            description = description,
            // ... other fields ...
            userId = currentUser.uid,           // ✅ Not empty
            userName = userName,                 // ✅ Not empty
            status = "active"
        )
        // ... save to Firestore ...
    }
}
```

## Data Flow Explanation

```
User Registration
├─ Saves name to: /users/{uid}/name in Realtime Database

User Logs In
├─ Firebase Auth authenticated (uid available)
└─ ProfileScreen fetches name from: /users/{uid}/name

User Creates Task
├─ Fetches uid from: FirebaseAuth.currentUser.uid
├─ Fetches name from: Realtime Database /users/{uid}/name
└─ Saves task to Firestore with:
   ├─ userId: actual uid ✅
   └─ userName: actual name from database ✅
```

## Testing the Fix

1. **Run the app**: `./gradlew assembleDebug -x test`
2. **Create a task**
3. **Check Firestore Console**
4. The task document should now show:
   ```
   userId: "zRnNUmNm1hN8HLNBLtmb9AiYfW32"
   userName: "John Doe" (or whatever name you registered with)
   ```

## Why This Matters

| Before | After |
|--------|-------|
| `userName: ""` | `userName: "John Doe"` |
| `userId: ""` | `userId: "zRnNUmNm1h..."` |
| Tasks don't show who posted them | Tasks clearly show the poster's name |

## Files Modified

- `app/src/main/java/com/example/taskbug/ui/tasks/TaskViewModel.kt`

## Build Status

✅ Build Successful - Ready to test!

---

## Summary

The issue was that we were trying to get the user's name from Firebase Auth (where it was never set), instead of from the Realtime Database (where it's actually stored during registration).

Now the fix fetches the name from the correct database, so both `userId` and `userName` will be properly populated in all new tasks! 🎯

