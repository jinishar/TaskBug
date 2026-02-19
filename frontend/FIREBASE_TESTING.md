## Firebase Realtime Database - Quick Start Testing

### Step 1: Run Your App
1. Build and run your app in Android Studio
2. Open Android Studio's **Logcat** view at the bottom
3. Filter by "AuthViewModel" to see debug logs

### Step 2: Test Sign Up
1. Create a new account with:
   - Name: Test User
   - Email: test@example.com
   - Phone: 1234567890
   - Password: Test123!

2. **Check Logcat** for messages like:
   ```
   D/AuthViewModel: Starting sign up for email: test@example.com
   D/AuthViewModel: User created: [long uid string]
   D/AuthViewModel: User profile saved successfully to database
   ```

3. **Check Firebase Console**:
   - Go to firebase.google.com
   - Select project "taskbugcu"
   - Click "Realtime Database"
   - Click "Data" tab
   - You should see:
   ```
   users/
     [uid]/
       email: "test@example.com"
       location: ""
       name: "Test User"
       phone: "1234567890"
       uid: "[same as parent]"
   ```

### Step 3: Test Sign In
1. Sign out (if logged in)
2. Sign in with the same email/password
3. **Check Logcat** for:
   ```
   D/AuthViewModel: Starting sign in for email: test@example.com
   D/AuthViewModel: Sign in successful, fetching profile...
   D/AuthViewModel: Profile loaded successfully: Test User
   ```

### Step 4: Test Profile Update (Location)
1. After signing in, update your location
2. **Check Logcat** for:
   ```
   D/AuthViewModel: Updating location for user: [uid]
   D/AuthViewModel: Location updated successfully to: [location]
   ```
3. **Check Firebase Console** - the "location" field should be updated

### Troubleshooting: If You See Errors

**Error: "Permission denied" / "PERMISSION_DENIED"**
- Firebase Realtime Database security rules need updating
- Follow steps in FIREBASE_SETUP.md "Database Security Rules" section

**Error: "Could not connect"**
- Check internet connection
- Verify INTERNET permission in AndroidManifest.xml (✅ Added)
- Verify database URL is correct: `https://taskbugcu-default-rtdb.asia-southeast1.firebasedatabase.app`

**Error: "User profile is null"**
- Check if user was created successfully in first step
- Verify data exists in Firebase Console
- Check if UserProfile data class matches stored data structure

**No logs appearing**
- Make sure Logcat is set to "Verbose" or "Debug" level
- Filter by "AuthViewModel" tag
- Try searching for "Firebase" in Logcat

### Files Modified
✅ app/build.gradle.kts - Added Firebase dependencies
✅ app/src/main/AndroidManifest.xml - Added INTERNET permission  
✅ app/src/main/java/com/example/taskbug/ui/auth/AuthViewModel.kt - Enhanced with proper error handling

### Next: Advanced Testing
- Test offline mode (toggle airplane mode)
- Test with multiple accounts
- Test data persistence after restart

