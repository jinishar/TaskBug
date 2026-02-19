# Troubleshooting Checklist - Complete Flow

## 🔍 DIAGNOSE THE ISSUE

### Check 1: Are Firebase Rules Published?

**How to verify:**
1. Firebase Console → Realtime Database → Rules tab
2. Do you see this?
```json
{
  "rules": {
    "users": {
      "$uid": {
        ".read": "auth.uid === $uid",
        ".write": "auth.uid === $uid"
      }
    }
  }
}
```

**Result:**
- ✅ YES → Go to Check 2
- ❌ NO → **UPDATE RULES NOW** (see `STEP_BY_STEP_RULES_FIX.md`)

---

### Check 2: Did You Restart App After Updating Rules?

**Important:** Firebase rules take 1-2 minutes to apply

**What to do:**
1. After updating rules in Firebase Console
2. Close the app completely
3. Clear app data: Settings → Apps → TaskBug → Storage → Clear Data
4. Restart the app
5. Try signing up again

**Result:**
- ✅ Done → Go to Check 3
- ❌ Skipped → Do it now!

---

### Check 3: Check Logcat During Sign Up

**How to monitor:**
1. Open Android Studio
2. Open Logcat tab (bottom)
3. Filter by: "AuthViewModel"
4. Start a fresh sign up

**Expected messages:**
```
D/AuthViewModel: Starting sign up for email: test@example.com
D/AuthViewModel: User created: [long_uid], saving to database...
D/AuthViewModel: User profile saved successfully to database
```

**What to look for:**

| Message | Meaning |
|---------|---------|
| `Starting sign up` | Request received ✅ |
| `User created` | Account made ✅ |
| `saved successfully` | Data saved to database ✅ |
| `Permission denied` | Rules still blocking ❌ |
| `Could not connect` | Network issue ❌ |
| `Email already exists` | Try different email ❌ |

**Result:**
- ✅ See "saved successfully" → Go to Check 4
- ❌ See "Permission denied" → Rules not applied, see Check 1 again
- ❌ See "Could not connect" → Check internet

---

### Check 4: Verify Data in Firebase Console

**How to check:**
1. Firebase Console → taskbugcu → Realtime Database → Data tab
2. Look for "users" node
3. Expand it
4. Look for your user ID

**Expected structure:**
```
users/
  abc123xyz789/
    ├── email: "test@example.com"
    ├── location: ""
    ├── name: "Test User"
    ├── phone: "1234567890"
    └── uid: "abc123xyz789"
```

**Result:**
- ✅ Data visible → Go to Check 5
- ❌ Empty/null → Data save failed, check Logcat error
- ❌ Wrong data → Wrong user selected, try different email

---

### Check 5: Check Profile Screen Display

**What to do:**
1. After successful sign up, dashboard appears
2. Click "Profile" tab (bottom right)
3. Wait 2-3 seconds for data to load

**Expected:**
```
[Avatar image showing]
"Loading..." briefly then disappears
Your Name appears
Your Email appears
Your Phone appears
All fields populated
```

**Result:**
- ✅ All data visible → SUCCESS! ✅✅✅
- ❌ Still "Loading..." → Refresh not triggering, see below
- ❌ Empty fields → Profile not loading data
- ❌ Wrong data → Cache issue, clear data

---

## 🚨 TROUBLESHOOTING BY ERROR

### Error 1: "Permission Denied" in Logcat

**Cause:** Security rules still blocking writes

**Fix:**
1. Verify rules are updated in Firebase Console
2. Rules must be published (check for green checkmark)
3. Restart app after publishing
4. Try signing up again

**Test:**
```
Logcat filter: "AuthViewModel"
Sign up → Look for error message
If still "Permission denied" → Rules not applied
```

---

### Error 2: "Could Not Connect to Database"

**Cause:** Network issue or wrong database URL

**Possible fixes:**
1. Check internet connection
2. Verify database URL is correct:
   ```
   https://taskbugcu-default-rtdb.asia-southeast1.firebasedatabase.app
   ```
3. Try using WiFi instead of mobile data
4. Restart phone

**Test:**
```
Logcat filter: "Database"
Check for connection errors
```

---

### Error 3: Profile Screen Still Shows "Loading..."

**Cause:** Data not fetching properly

**Possible fixes:**
1. Check Logcat for "Fetching profile" errors
2. Verify data exists in Firebase Console
3. Clear app data and sign in again
4. Close and reopen app

**Test:**
```
Logcat filter: "AuthViewModel"
Navigate to Profile
Look for: "Fetching profile for user:"
If error message → identify the problem
```

---

### Error 4: Data in Firebase But Profile Empty

**Cause:** Profile screen not refreshing

**Possible fixes:**
1. Close and reopen app
2. Sign out and sign back in
3. Check Logcat for "refreshUserProfile" message
4. Verify ProfileScreen.kt has LaunchedEffect

**Test:**
```
Close app completely
Reopen
Navigate to Profile
See if data loads from cache
```

---

### Error 5: "Email Already Exists"

**Cause:** Using same email twice

**Fix:** Use different email for each test:
```
Test 1: test1@example.com
Test 2: test2@example.com
Test 3: test3@example.com
etc.
```

---

## 📋 QUICK DIAGNOSTIC STEPS

### Quick Check (2 minutes):
```
1. Logcat filter: "AuthViewModel"
2. Sign up with test email
3. Watch for 3 messages
4. First message = "Starting sign up"
5. Last message = "saved successfully" OR error
6. See error? Go to that section above
```

### Full Verification (5 minutes):
```
1. Check Firebase Rules (Check 1)
2. Restart app (Check 2)
3. Watch Logcat (Check 3)
4. Verify Firebase Data (Check 4)
5. Check Profile Screen (Check 5)
```

---

## ✅ WHAT SUCCESS LOOKS LIKE

### In Logcat:
```
✅ D/AuthViewModel: User profile saved successfully to database
✅ D/AuthViewModel: Profile loaded successfully: TestUser
✅ No error messages
✅ No "Permission denied"
```

### In Firebase Console:
```
✅ Data tab shows "users" node
✅ User ID visible
✅ All fields populated (name, email, phone, etc.)
✅ Data structure matches expectation
```

### On Profile Screen:
```
✅ Brief "Loading..." appears
✅ Name displays
✅ Email displays
✅ Phone displays
✅ Location displays (even if empty)
✅ All fields filled (not blank)
```

---

## 📞 IF YOU'RE STILL STUCK

**Collect this information:**

1. **Screenshot of Firebase Console → Rules tab**
   - Showing the rules you updated

2. **Screenshot of Firebase Console → Data tab**
   - Showing the "users" node and your data

3. **Logcat output (5 lines around the error)**
   - Filter by "AuthViewModel"
   - Copy the error message

4. **Screenshot of Profile Screen**
   - Showing what's displayed

5. **Step-by-step what you did**
   - Did you update rules?
   - Did you restart app?
   - What email did you use?

Then I can help you directly!

---

## 🎯 SUCCESS CHECKLIST

Mark these off as you complete:

- [ ] Firebase rules updated
- [ ] Rules published successfully
- [ ] App closed and reopened
- [ ] App data cleared
- [ ] Fresh sign up completed
- [ ] Logcat shows "saved successfully"
- [ ] Firebase Console shows user data
- [ ] Profile screen shows name
- [ ] Profile screen shows email
- [ ] Profile screen shows phone
- [ ] No error messages anywhere

✅ All checked? **DONE! Success! 🎉**

---

**Start from the top and work through each check systematically!**

