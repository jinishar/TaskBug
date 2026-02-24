# Firestore Security Rules - Copy This Exactly

## IMPORTANT: Copy these rules to your Firebase Console

1. Go to: Firebase Console → Firestore Database → Rules
2. Delete all existing rules
3. Paste the entire code below
4. Click Publish

---

## Complete Firestore Rules

Copy everything between the three backticks:

```
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

## Step-by-Step Instructions

1. Open https://console.firebase.google.com
2. Select your project
3. Click "Firestore Database" in left menu
4. Click "Rules" tab at top
5. Select ALL existing text and delete
6. Copy the rules code above (between backticks)
7. Paste into the rules editor
8. Click "PUBLISH" button
9. Wait for confirmation message
10. Wait 60 seconds for rules to activate

---

## Verify It Works

After publishing:
- Look for: "Rules last updated: [timestamp]"
- Should see green checkmark
- No red error text

---

## Testing

1. Rebuild app: ./gradlew clean build
2. Log in with any registered user
3. Create a task
4. Should see "Task posted successfully!"
5. Task appears in feed

---

## Common Issues

If you get "Parse error":
- Make sure you copied the entire code block
- Check for extra spaces or special characters
- Try copying and pasting again

If you still get "Permission denied":
- Wait 60 seconds after publishing
- Rebuild the app
- Make sure user is logged in
- Try creating task again

