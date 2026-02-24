# Firestore Security Rules - Copy This Exactly

## ⚠️ IMPORTANT: Copy these rules to your Firebase Console

1. Go to: **Firebase Console → Firestore Database → Rules**
2. **Delete all existing rules**
3. **Paste the entire code below**
4. Click **Publish**

---

## Complete Firestore Rules

```firestore
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {
    
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
    
    // Users collection (if you have one)
    match /users/{userId} {
      allow read, update, delete: if request.auth.uid == userId;
      allow create: if request.auth.uid != null;
    }
  }
}
```

---

## What Changed

**Before:**
```firestore
allow create: if request.auth != null && 
                 request.resource.data.userId == request.auth.uid;
```

**After:**
```firestore
allow create: if request.auth != null;
```

✅ **Now all authenticated users can create tasks!**

---

## If You Still Get "Permission denied"

### CRITICAL: Must do this after updating rules

1. Open **Firebase Console** → **Firestore Database** → **Rules**
2. **Copy & Paste** the rules above exactly
3. Click **PUBLISH** button
4. **WAIT 30-60 SECONDS** for rules to activate
5. Rebuild app: `./gradlew build`
6. Test creating a task

### Verify Rules Are Published
- Look for: **"Rules last updated: [timestamp]"**
- If not there, click **PUBLISH** again

---

## Common Issues & Solutions

| Error | Cause | Solution |
|-------|-------|----------|
| PERMISSION_DENIED | Old rules still active | Wait 60 seconds after publishing |
| PERMISSION_DENIED | Rules not published | Check for green checkmark, click PUBLISH |
| PERMISSION_DENIED | User not logged in | Log in first, then try creating task |
| Rules error on publish | Syntax error | Copy rules exactly as shown above |

---

**COPY THE RULES ABOVE TO FIREBASE CONSOLE NOW!**



