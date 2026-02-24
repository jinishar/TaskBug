# ✅ COMPLETE FIRESTORE RULES - Tasks + Users

## COPY THIS EXACTLY TO FIRESTORE CONSOLE

Go to: **Firebase Console → Firestore Database → Rules** and paste everything below:

```
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {
    
    // ===== TASKS COLLECTION =====
    // Allow all authenticated users to read and create tasks
    match /tasks/{document=**} {
      // Allow any authenticated user to create tasks
      allow create: if request.auth != null;
      
      // Allow any authenticated user to read all tasks
      allow read: if request.auth != null;
      
      // Only owner can update their own task
      allow update: if request.auth != null && 
                       request.auth.uid == resource.data.userId;
      
      // Only owner can delete their own task
      allow delete: if request.auth != null && 
                       request.auth.uid == resource.data.userId;
    }
    
    // ===== USERS COLLECTION =====
    // User profiles stored in Firestore (optional - you're using Realtime DB for this)
    match /users/{userId} {
      // User can read their own profile
      allow read: if request.auth != null && request.auth.uid == userId;
      
      // User can update their own profile
      allow update: if request.auth != null && request.auth.uid == userId;
      
      // User can delete their own profile
      allow delete: if request.auth != null && request.auth.uid == userId;
      
      // Anyone authenticated can create a user document
      allow create: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

---

## STEP-BY-STEP TO APPLY RULES

1. Open: https://console.firebase.google.com
2. Select your project
3. Go to: **Firestore Database** (left menu)
4. Click: **Rules** tab (at top)
5. Delete ALL existing text
6. Copy the code above (between the triple backticks)
7. Paste into the editor
8. Click: **PUBLISH** button
9. Wait for: "Rules last updated: [timestamp]" message
10. Wait: 60 seconds for rules to activate globally

---

## WHAT THESE RULES ALLOW

### TASKS Collection
| Action | Who | Allowed |
|--------|-----|---------|
| Create Task | Any logged-in user | ✅ YES |
| Read Tasks | Any logged-in user | ✅ YES |
| Update Task | Owner only | ✅ YES |
| Delete Task | Owner only | ✅ YES |

### USERS Collection
| Action | Who | Allowed |
|--------|-----|---------|
| Create Profile | Self only | ✅ YES |
| Read Profile | Self only | ✅ YES |
| Update Profile | Self only | ✅ YES |
| Delete Profile | Self only | ✅ YES |

---

## IMPORTANT NOTE

Your app currently uses **Realtime Database** for user profiles, NOT Firestore.

- ✅ **Task data** → Firestore (uses these rules)
- ✅ **Profile data** → Realtime Database (uses JSON rules you set before)
- ✅ **Auth data** → Firebase Authentication

So the "users" collection in these rules is optional. Your current setup works fine with:
- **Firestore**: Tasks only
- **Realtime Database**: Users/Profiles only

---

## TESTING AFTER APPLYING RULES

1. Run: `./gradlew clean build`
2. Install and run app
3. Log in with a registered user
4. Create a task
5. Should see: "Task posted successfully!" ✅
6. Task should appear in feed ✅

---

## IF YOU GET ERRORS

| Error | Solution |
|-------|----------|
| "Parse error in rules" | Copy the entire code block again, check for typos |
| "Permission denied" | Wait 60 seconds, rebuild app, ensure user is logged in |
| "Rules won't publish" | Check syntax - make sure no extra characters |

---

## SUMMARY

✅ **Tasks**: All authenticated users can create and read
✅ **Users**: Each user can only manage their own profile
✅ **Security**: Enforced at the database level, not just client-side

