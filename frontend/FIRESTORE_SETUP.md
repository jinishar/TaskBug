# Firestore Configuration & Setup

## 📋 Security Rules

Copy this into **Firebase Console → Firestore → Rules** tab:

```firestore
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Tasks Collection Rules
    match /tasks/{document=**} {
      // Anyone authenticated can create tasks
      allow create: if request.auth != null 
        && request.resource.data.userId == request.auth.uid
        && request.resource.data.keys().hasAll(['title', 'description', 'userId', 'userName']);
      
      // Anyone authenticated can read tasks
      allow read: if request.auth != null;
      
      // Only the task owner can update
      allow update: if request.auth.uid == resource.data.userId;
      
      // Only the task owner can delete
      allow delete: if request.auth.uid == resource.data.userId;
    }
    
    // Users Collection Rules (if using separate users collection)
    match /users/{userId} {
      allow read, update, delete: if request.auth.uid == userId;
      allow create: if request.auth.uid != null;
    }
    
    // Deny all other access
    match /{document=**} {
      allow read, write: if false;
    }
  }
}
```

## 🗂️ Firestore Indexes

### Required Indexes

**Index 1: For fetching active tasks (sorted by date)**
```
Collection ID: tasks
Fields:
  - status (Ascending)
  - createdAt (Descending)
Status: Auto-created by Firebase
Query: 
  WHERE status == "active" ORDER BY createdAt DESC
```

**Index 2: For fetching user's own tasks**
```
Collection ID: tasks
Fields:
  - userId (Ascending)
  - createdAt (Descending)
Status: Auto-created by Firebase
Query:
  WHERE userId == "user_id" ORDER BY createdAt DESC
```

**Optional Index: For filtered searches**
```
Collection ID: tasks
Fields:
  - status (Ascending)
  - category (Ascending)
  - createdAt (Descending)
Query:
  WHERE status == "active" AND category == "Shopping" ORDER BY createdAt DESC
```

### How to Create Indexes Manually
1. Go to Firebase Console
2. Select your project
3. Navigate to Firestore Database
4. Go to "Indexes" tab
5. Click "Create Index"
6. Select collection, fields, and sort order
7. Click "Create"

### Firebase Auto-Creation
- Firestore will **auto-prompt** when you first run a query that needs an index
- Click the link in the error message to auto-create it
- Takes 1-5 minutes to build

## 📊 Sample Data Structure

### Single Task Document
```json
{
  "id": "auto-generated-by-firestore",
  "title": "Groceries Delivery",
  "description": "Need someone to pick up milk, bread, and eggs from the local supermarket",
  "category": "Shopping",
  "deadline": "Oct 25, 5:00 PM",
  "pay": 1200,
  "location": "Brooklyn, NY",
  "userId": "user_uid_123abc",
  "userName": "John Doe",
  "createdAt": "2024-10-23T10:30:00Z",
  "status": "active",
  "imageUrl": "https://storage.googleapis.com/..."
}
```

### Tasks Collection Tree
```
firestore/
├── tasks/ (collection)
│   ├── abc123def456/ (document)
│   │   ├── title: "Groceries Delivery"
│   │   ├── description: "Need someone..."
│   │   ├── category: "Shopping"
│   │   ├── deadline: "Oct 25, 5:00 PM"
│   │   ├── pay: 1200
│   │   ├── location: "Brooklyn, NY"
│   │   ├── userId: "user_uid_123abc"
│   │   ├── userName: "John Doe"
│   │   ├── createdAt: timestamp(2024-10-23 10:30:00)
│   │   ├── status: "active"
│   │   └── imageUrl: ""
│   │
│   └── ghi789jkl012/ (document)
│       ├── title: "Furniture Assembly"
│       ├── description: "Help assemble IKEA desk..."
│       └── ...
```

## 🔍 Query Examples

### Query 1: Get All Active Tasks (Real-time)
```kotlin
tasksCollection
  .whereEqualTo("status", "active")
  .orderBy("createdAt", Query.Direction.DESCENDING)
  .addSnapshotListener { snapshot, error ->
    // Handle real-time updates
  }
```

### Query 2: Get User's Own Tasks
```kotlin
tasksCollection
  .whereEqualTo("userId", userId)
  .orderBy("createdAt", Query.Direction.DESCENDING)
  .addSnapshotListener { snapshot, error ->
    // Handle real-time updates
  }
```

### Query 3: Get Tasks by Category
```kotlin
tasksCollection
  .whereEqualTo("status", "active")
  .whereEqualTo("category", "Shopping")
  .orderBy("createdAt", Query.Direction.DESCENDING)
  .limit(10)
  .get()
```

### Query 4: Get High-Pay Tasks
```kotlin
tasksCollection
  .whereEqualTo("status", "active")
  .whereGreaterThanOrEqualTo("pay", 1000)
  .orderBy("pay", Query.Direction.DESCENDING)
  .limit(20)
  .get()
```

## 🔐 Security Best Practices

### ✅ DO:
- ✅ Validate user authentication before any operation
- ✅ Check userId matches in create/update/delete
- ✅ Use Firestore rules to enforce security
- ✅ Never expose user UIDs in plain URLs
- ✅ Validate input data before saving
- ✅ Use ServerTimestamp to prevent clock skew
- ✅ Limit query results with .limit()

### ❌ DON'T:
- ❌ Allow read/write without authentication
- ❌ Trust client-side ownership checks alone
- ❌ Store sensitive data (passwords, tokens)
- ❌ Allow unlimited query results
- ❌ Use client-generated timestamps
- ❌ Expose admin credentials
- ❌ Log sensitive user data

## 📈 Performance Optimization

### Query Optimization
```kotlin
// ❌ BAD: Gets all tasks, then filters in code
tasksCollection
  .get()
  .continueWith { snapshot ->
    snapshot.documents.filter { it.data["status"] == "active" }
  }

// ✅ GOOD: Filters at database level
tasksCollection
  .whereEqualTo("status", "active")
  .get()
```

### Pagination
```kotlin
// First page
var query: Query = tasksCollection
  .whereEqualTo("status", "active")
  .orderBy("createdAt", Query.Direction.DESCENDING)
  .limit(10)

query.get().addOnSuccessListener { snapshot ->
  val tasks = snapshot.toObjects(Task::class.java)
  
  // Get next page using last document
  if (snapshot.documents.isNotEmpty()) {
    val lastDocument = snapshot.documents[snapshot.documents.size - 1]
    val nextQuery = tasksCollection
      .whereEqualTo("status", "active")
      .orderBy("createdAt", Query.Direction.DESCENDING)
      .startAfter(lastDocument)
      .limit(10)
  }
}
```

### Real-time Listener Optimization
```kotlin
// ✅ GOOD: Only listen when needed, unsubscribe when done
val subscription = tasksCollection
  .whereEqualTo("status", "active")
  .addSnapshotListener { snapshot, error ->
    // Update UI
  }

// Unsubscribe when activity/fragment is destroyed
subscription.remove()

// ✅ GOOD: Use Flow for automatic cleanup
fun getActiveTasks(): Flow<List<Task>> = callbackFlow {
  val listener = tasksCollection.addSnapshotListener { snapshot, error ->
    // Send to collector
    trySend(tasks)
  }
  awaitClose { listener.remove() } // Auto cleanup
}
```

## 💰 Cost Optimization

### Pricing Model
- **Read**: 1 read per document
- **Write**: 1 write per document
- **Delete**: 1 write
- **Real-time updates**: 1 read per update

### Optimize Costs
1. **Batch Operations**: Combine multiple writes
2. **Query Efficiently**: Use indexes for filters
3. **Limit Results**: Use .limit() clause
4. **Pagination**: Don't load all at once
5. **Caching**: Cache results locally
6. **Compression**: Compress data fields

### Cost Estimate
```
Scenario: 1000 users, 100 tasks created per day, 10 updates per task

Daily Operations:
- Create: 100 writes × 100 bytes = 10 KB writes cost
- Read (feed updates): 1000 users × 10 views/day × 100 tasks = high read cost
- Updates: 100 tasks × 10 updates = 1000 write cost

Monthly Estimate:
- Free tier: 50,000 reads/writes
- First 100,000 reads/writes: ~$0.06
- High-traffic app: $5-50/month
```

## 🚀 Deployment Checklist

### Before Going Live
- [ ] Firestore rules properly set
- [ ] Indexes created and built
- [ ] Security rules tested
- [ ] Pagination implemented
- [ ] Error handling in place
- [ ] Logging enabled for debugging
- [ ] Performance tested with 1000+ documents
- [ ] Backup enabled (export/import)
- [ ] Monitoring set up (Firebase console)

### Firebase Rules Testing
Use Firebase Rules Playground:
1. Go to Firestore → Rules
2. Click "Rules playground"
3. Test different auth states
4. Test different user IDs
5. Verify rules work as expected

### Sample Test Cases
```
Test 1: Unauthenticated user
- Expected: BLOCKED ❌
- Action: Try to read/write tasks

Test 2: Authenticated user creating task
- Expected: ALLOWED ✅
- Action: Create with valid data

Test 3: Authenticated user deleting own task
- Expected: ALLOWED ✅
- Action: Delete with userId match

Test 4: Authenticated user deleting other's task
- Expected: BLOCKED ❌
- Action: Delete with different userId
```

## 🔄 Backup & Recovery

### Enable Automatic Backups
```
Firebase Console → Firestore Database → Settings → Automatic backups
```

### Manual Export
```bash
gcloud firestore export gs://your-bucket/backup-$(date +%s)
```

### Restore from Backup
```bash
gcloud firestore import gs://your-bucket/backup-timestamp/
```

## 📊 Monitoring & Analytics

### Key Metrics to Monitor
1. **Read Operations**: Ensure efficient queries
2. **Write Operations**: Monitor for data corruption
3. **Stored Data**: Track storage usage
4. **Network Traffic**: Monitor bandwidth
5. **Latency**: Track response times
6. **Errors**: Monitor for failed operations

### Enable Firebase Analytics
```kotlin
// Automatic with Firebase SDK
// View in Firebase Console → Analytics
```

## 🧪 Testing Firestore Locally

### Using Firebase Emulator
```bash
# Install Firebase CLI
npm install -g firebase-tools

# Start emulator
firebase emulators:start

# Connect in code
if (BuildConfig.DEBUG) {
    val settings = firestoreSettings {
        // Use emulator
        host = "localhost:8080"
        isSslEnabled = false
    }
    FirebaseFirestore.getInstance().firestoreSettings = settings
}
```

---

## 📞 Troubleshooting

### Issue: "Missing or insufficient permissions"
**Cause**: Security rules blocking operation
**Solution**: Review rules, check userId matches

### Issue: "No matching index"
**Cause**: Query needs composite index
**Solution**: Firebase prompts you to create. Click link.

### Issue: "Operation timed out"
**Cause**: Large dataset or poor network
**Solution**: Add .limit(), implement pagination

### Issue: "Document too large"
**Cause**: Storing >1MB in single document
**Solution**: Split into subcollections

---

**Last Updated**: February 20, 2026  
**Firestore Version**: v1  
**Status**: Production Ready ✅

