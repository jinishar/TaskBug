# Firebase Realtime Database Setup Guide

## ✅ What We've Fixed

### 1. **Dependencies**
- Added `firebase-database-ktx` (Kotlin extensions)
- Added `firebase-database` explicitly
- Added `kotlinx-coroutines-play-services` for proper coroutine support
- Added `firebase-analytics-ktx` for debugging

### 2. **Code Improvements**
- ✅ Enhanced error handling with detailed logging
- ✅ Added proper serialization support for UserProfile data class
- ✅ Improved database initialization with fallback mechanisms
- ✅ Added offline persistence support
- ✅ Better error messages for debugging

### 3. **Permissions**
- ✅ Added `INTERNET` permission (critical for Firebase)
- ✅ Added `ACCESS_NETWORK_STATE` permission
- ✅ Verified location permissions

---

## 🔧 Important Configuration Steps

### Step 1: Verify Firebase Project Setup
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project: **taskbugcu**
3. Go to **Realtime Database** section
4. Confirm your database URL matches: `https://taskbugcu-default-rtdb.asia-southeast1.firebasedatabase.app`

### Step 2: Database Security Rules
Your Realtime Database needs proper security rules to allow write operations:

```json
{
  "rules": {
    "users": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid",
        ".validate": "newData.hasChildren(['name', 'email', 'phone', 'location', 'uid'])"
      }
    }
  }
}
```

**To apply these rules:**
1. Go to Firebase Console
2. Select Realtime Database
3. Click "Rules" tab
4. Replace the default rules with the above
5. Click "Publish"

### Step 3: Verify google-services.json
The file `app/google-services.json` should contain:
```json
{
  "project_info": {
    "project_id": "taskbugcu",
    ...
  },
  "client": [
    {
      "client_info": {
        "package_name": "com.example.taskbug"
      },
      ...
    }
  ]
}
```

---

## 🚀 Testing Firebase Connection

### Method 1: Using Logcat
1. Run your app
2. In Android Studio, go to **Logcat** tab
3. Filter by tag: `AuthViewModel`
4. You should see logs like:
   - "Starting sign up for email: test@example.com"
   - "User created: [uid]"
   - "User profile saved successfully to database"

### Method 2: Firebase Console
1. Go to Firebase Console → Realtime Database
2. After signing up, you should see a "users" node
3. Expand it to see: `users → [uid] → {name, email, phone, location, uid}`

### Method 3: Test API Call
```kotlin
// In your app, after signup:
val user = Firebase.auth.currentUser
val ref = FirebaseDatabase.getInstance().getReference("users/${user?.uid}")
ref.get().addOnSuccessListener { snapshot ->
    Log.d("TEST", "Data: ${snapshot.value}")
}
```

---

## ⚠️ Common Issues & Solutions

### Issue 1: "Permission Denied" Error
**Cause:** Database rules don't allow write access  
**Solution:** Update security rules as shown in Step 2

### Issue 2: "Could not connect to the database"
**Cause:** INTERNET permission missing or wrong database URL  
**Solution:** 
- Verify AndroidManifest.xml has INTERNET permission ✅ (Done)
- Verify database URL in AuthViewModel matches your Firebase project

### Issue 3: "null reference" when reading profile
**Cause:** Data wasn't saved to database  
**Solution:**
- Check Firebase Console to confirm data exists
- Verify UserProfile data class matches database structure
- Check Logcat for error messages

### Issue 4: "PERMISSION_DENIED" on database write
**Cause:** User not authenticated or security rules too strict  
**Solution:**
- Verify user is signed in before writing
- Update database security rules
- Check firebase console rules tab

---

## 📱 Testing Checklist

- [ ] App installs and runs without errors
- [ ] Sign up creates a new user and saves to database
- [ ] Check Firebase Console → Realtime Database shows user data
- [ ] Sign in retrieves user profile successfully
- [ ] Location update saves to database
- [ ] Logcat shows proper debug messages (search for "AuthViewModel")
- [ ] No "Permission Denied" errors in Logcat
- [ ] Profile loads after restart (offline persistence)

---

## 🔍 Debugging Tips

### Enable Firebase Debug Logging
Add this to your MainActivity.onCreate():
```kotlin
FirebaseDatabase.getInstance().setLogLevel(com.google.firebase.database.Logger.Level.DEBUG)
```

### Check Network in Logcat
```
adb logcat | grep -i firebase
```

### Manual Data Verification
In Firebase Console:
1. Realtime Database → Data tab
2. Should show structure:
```
users/
  user_uid_1/
    name: "John Doe"
    email: "john@example.com"
    phone: "1234567890"
    location: ""
    uid: "user_uid_1"
```

---

## ✅ All Changes Made

1. ✅ **build.gradle.kts** - Added Firebase database and coroutines dependencies
2. ✅ **AndroidManifest.xml** - Added INTERNET and ACCESS_NETWORK_STATE permissions
3. ✅ **AuthViewModel.kt** - Enhanced with:
   - Better error handling
   - Comprehensive logging
   - Proper offline persistence
   - uid field in UserProfile for data validation

---

## 🎯 Next Steps

1. **Test the app** with the changes
2. **Check Logcat** for "AuthViewModel" tags
3. **Verify Firebase Console** shows your data
4. **Update database rules** if you see permission errors
5. **Report any specific errors** from Logcat for further debugging

**If you still see issues, collect the error message from Logcat and let me know!**

