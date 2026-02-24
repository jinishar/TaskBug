# 🔐 REALTIME DATABASE RULES FOR USER PROFILES

## Copy-Paste These Rules Now

Go to: **Firebase Console → Realtime Database → Rules**

```json
{
  "rules": {
    "users": {
      "$uid": {
        ".read": "auth != null && auth.uid == $uid",
        ".write": "auth != null && auth.uid == $uid",
        ".validate": "newData.hasChildren(['name', 'email', 'phone', 'uid'])"
      }
    }
  }
}
```

---

## What This Allows

✅ **Each user can READ their own profile only**
✅ **Each user can WRITE/UPDATE their own profile only**
✅ **Profile must have: name, email, phone, uid**
✅ **Users cannot see other users' profiles**
✅ **Users cannot modify other users' data**

---

## Steps to Apply

1. Open: https://console.firebase.google.com
2. Select your project
3. Click **"Realtime Database"** (left menu)
4. Click **"Rules"** tab (at top)
5. Delete ALL existing text
6. Copy the rules code above (between the triple backticks)
7. Paste into the editor
8. Click **"PUBLISH"** button
9. Wait for: "Rules updated at [timestamp]" message
10. Wait: 60 seconds for global activation

---

## Data Structure Expected

After applying these rules, your Realtime Database should look like:

```
users/
├── uid_001/
│   ├── name: "John Doe"
│   ├── email: "john@example.com"
│   ├── phone: "+1234567890"
│   ├── location: "Brooklyn, NY"
│   └── uid: "uid_001"
│
├── uid_002/
│   ├── name: "Jane Smith"
│   ├── email: "jane@example.com"
│   ├── phone: "+9876543210"
│   ├── location: "Manhattan, NY"
│   └── uid: "uid_002"
```

---

## Rule Breakdown

| Rule | Meaning |
|------|---------|
| `".read": "auth != null && auth.uid == $uid"` | Only authenticated users can read **their own** profile |
| `".write": "auth != null && auth.uid == $uid"` | Only authenticated users can write to **their own** profile |
| `".validate": "newData.hasChildren(['name', 'email', 'phone', 'uid'])"` | New profile must contain required fields |

---

## Testing

1. Register a new user → Data saved to `/users/{uid}`
2. Log in → Profile loads from `/users/{uid}`
3. Update location → Data updates in `/users/{uid}/location`
4. Try to access another user's profile → Should fail ✅

---

## IMPORTANT - Two Databases

Your app uses:

| Database | Data | Rules File |
|----------|------|-----------|
| **Realtime Database** | User profiles (name, email, phone, location) | This file ↑ |
| **Firestore** | Tasks (title, description, deadline) | Use FIRESTORE_RULES_FINAL.md |

Apply rules to BOTH databases!

---

## If Rules Won't Publish

- Check for extra spaces or special characters
- Make sure JSON is valid (use validator if needed)
- Copy the entire code block again
- Make sure you're in **Realtime Database**, not Firestore

---

## Summary

✅ **Realtime Database rules** → Secures user profile data
✅ **Each user manages only their own profile**
✅ **Required fields**: name, email, phone, uid
✅ **Privacy**: Users can't see other users' data

