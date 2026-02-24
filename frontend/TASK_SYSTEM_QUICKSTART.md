# Task System - Quick Start Integration

## 🚀 5-Minute Setup

### Step 1: Verify All Files Exist
```
✅ app/src/main/java/com/example/taskbug/
   ├── model/Task.kt
   ├── data/repository/TaskRepository.kt
   ├── ui/tasks/TaskViewModel.kt
   ├── ui/tasks/TaskScreen.kt
   ├── ui/screens/AddTaskScreen.kt
   └── ui/screens/TaskFeedScreen.kt
```

### Step 2: Update Navigation
In your `AppNavGraph.kt` or navigation file, ensure:

```kotlin
composable("tasks") {
    TasksScreen()
}
```

### Step 3: Build & Run
```bash
./gradlew build
./gradlew installDebug
# Run on emulator/device
```

### Step 4: Set Firestore Rules
Copy-paste into Firebase Console → Firestore → Rules:

```firestore
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /tasks/{document=**} {
      allow create: if request.auth != null;
      allow read: if request.auth != null;
      allow update, delete: if request.auth.uid == resource.data.userId;
    }
  }
}
```

### Step 5: Create Indexes (if needed)
Firebase will prompt you to create indexes when queries fail. Create these:

1. Collection: `tasks`
   - Field: `status` (Ascending)
   - Field: `createdAt` (Descending)

2. Collection: `tasks`
   - Field: `userId` (Ascending)
   - Field: `createdAt` (Descending)

## ✨ Feature Overview

### What You Get:
1. **Real-time Task Feed** - Tasks appear instantly for all users
2. **Task Creation** - Beautiful form with validation
3. **Task Management** - Edit/delete own tasks
4. **Ownership Control** - Only owners can modify tasks
5. **Error Handling** - Graceful error messages
6. **Loading States** - Visual feedback during operations

### Task Fields:
```
- Title (required)
- Description (required)
- Category (optional)
- Deadline (optional)
- Pay/Reward in ₹ (optional)
- Location (optional)
- Auto-filled: userId, userName, createdAt, status
```

## 🎯 User Flow

### User Creates Task:
```
1. Clicks FAB (+) on Tasks screen
2. Fills task form
3. Clicks "Post Task"
4. Sees success message
5. Task appears in feed instantly
```

### User Manages Task:
```
1. Finds own task in feed
2. Clicks task menu (⋮)
3. Selects Edit or Delete
4. Confirms action
5. Change appears instantly
```

### User Views Others' Tasks:
```
1. Sees all active tasks in feed
2. Can click to view details
3. Cannot edit/delete others' tasks
4. Edit/Delete buttons hidden
```

## 📱 Screenshots

### Task Feed Screen
```
┌─────────────────────────────┐
│ Active Tasks          [+]   │
├─────────────────────────────┤
│ [Task Card 1]               │
│ ├─ Groceries Delivery ₹1200│
│ ├─ Need someone to pick...  │
│ ├─ 🏷 Shopping              │
│ ├─ 📅 Oct 25, 5:00 PM       │
│ └─ 📍 Brooklyn, NY          │
├─────────────────────────────┤
│ [Task Card 2]               │
│ ├─ Furniture Assembly ₹2500 │
│ ├─ Help assemble IKEA...    │
│ ├─ 🏷 Home                  │
│ ├─ 📅 Oct 26, 10:00 AM      │
│ └─ 📍 Queens, NY            │
└─────────────────────────────┘
```

### Add Task Dialog
```
┌─────────────────────────────┐
│ Post a New Task          ✕   │
├─────────────────────────────┤
│ [📷 Upload Task Photo]      │
├─────────────────────────────┤
│ Task Title              [___]│
│ Description             [___]│
│ Category                [___]│
│ Pay (₹)                 [___]│
│ Date                    [___]│
│ Location                [___]│
├─────────────────────────────┤
│        [Post Task]          │
└─────────────────────────────┘
```

## 🔧 Customization

### Change Colors
Edit `AddTaskScreen.kt` and `TaskFeedScreen.kt`:
```kotlin
private val AppTeal = Color(0xFF0F766E)  // Change this
private val AppBackground = Color(0xFFF9FAFB)
```

### Add More Task Fields
1. Add to `Task.kt` data class
2. Add to form in `AddTaskScreen.kt`
3. Add to display in `TaskCard.kt`
4. Update `createTask()` in `TaskViewModel.kt`

### Change Task Status Values
Default: "active", "completed", "cancelled"

Update in:
- `TaskRepository.kt` - query filter
- `TaskViewModel.kt` - status logic
- Firestore rules

## 🐛 Common Issues

### Issue: "No matching documents found"
**Cause**: Firestore hasn't indexed the query
**Fix**: Firebase will prompt you. Create the composite index.

### Issue: Tasks not updating in real-time
**Cause**: Real-time listener might be closed
**Fix**: Check `awaitClose()` in `TaskRepository.kt`

### Issue: "Permission denied" on create
**Cause**: User not authenticated
**Fix**: Verify user is logged in before navigating to tasks

### Issue: Delete button shows for non-owners
**Cause**: Ownership check failed
**Fix**: Verify `task.userId == currentUserId` logic

## 📊 Firestore Collection Preview

After creating a task, your Firestore should look like:

```
tasks (collection)
├── abc123def456 (document)
│   ├── title: "Groceries Delivery" (string)
│   ├── description: "Need someone..." (string)
│   ├── category: "Shopping" (string)
│   ├── deadline: "Oct 25, 5:00 PM" (string)
│   ├── pay: 1200 (number)
│   ├── location: "Brooklyn, NY" (string)
│   ├── userId: "user_uid_123abc" (string)
│   ├── userName: "John Doe" (string)
│   ├── createdAt: 2024-10-23 10:30:00 (timestamp)
│   ├── status: "active" (string)
│   └── imageUrl: "" (string)
└── ghi789jkl012 (document)
    └── ...
```

## 🧪 Quick Test

1. **Run the app**
   ```bash
   ./gradlew run
   ```

2. **Navigate to Tasks**
   - Tap Tasks in bottom nav

3. **Create a Task**
   - Tap FAB (+)
   - Fill "Groceries Delivery" as title
   - Fill "Buy milk and bread" as description
   - Set pay to 500
   - Click "Post Task"
   - ✅ You should see success message
   - ✅ Task should appear in feed

4. **Delete the Task**
   - Click on your task
   - Tap menu (⋮)
   - Select Delete
   - Confirm
   - ✅ Task should disappear

5. **Check Firestore**
   - Open Firebase Console
   - Go to Firestore → tasks collection
   - ✅ Document should exist/disappear

## 📈 Next Features to Add

- [ ] **Task Images**: Upload to Firebase Storage
- [ ] **Task Search**: Search by title/location
- [ ] **Task Filter**: Filter by category/price
- [ ] **Task Applications**: Users apply to tasks
- [ ] **Notifications**: Push notifications for deadlines
- [ ] **Reviews**: Rate completed tasks
- [ ] **Payments**: Stripe integration
- [ ] **Offline Mode**: Local caching

## 🎓 Learning Resources

- **MVVM Architecture**: https://developer.android.com/jetpack/guide
- **Jetpack Compose**: https://developer.android.com/compose
- **Firestore**: https://firebase.google.com/docs/firestore
- **Coroutines**: https://kotlinlang.org/docs/coroutines-overview.html

## 🆘 Support

### Enable Debug Logging
Add to TaskRepository.kt or TaskViewModel.kt:
```kotlin
Log.d("DEBUG", "Message here")
```

View in Android Studio:
- Logcat tab → Filter: "TaskRepository" or "TaskViewModel"

### Check Firebase Connection
In Firebase Console:
1. Firestore → Database
2. Check "tasks" collection exists
3. Monitor read/write operations
4. Check for errors

### Network Issues
Enable offline persistence in TaskRepository:
```kotlin
firestore.firestoreSettings = 
    firestoreSettings { isPersistenceEnabled = true }
```

---

## ✅ Completion Checklist

After integration:
- [ ] App builds without errors
- [ ] Navigation includes Tasks screen
- [ ] Can create tasks successfully
- [ ] Can delete own tasks
- [ ] Can't delete others' tasks
- [ ] Real-time updates working
- [ ] Firestore rules set
- [ ] Indexes created (if prompted)
- [ ] All fields saving correctly
- [ ] Error handling working

**You're ready to go!** 🚀

---

**Last Updated**: February 20, 2026
**Version**: 1.0
**Status**: Production Ready ✅

