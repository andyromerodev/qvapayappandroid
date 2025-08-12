# Throttling System Detailed Logging Guide

## Overview
This guide explains how to monitor the throttling system using the comprehensive logging that has been added.

## Log Tags to Monitor

### 🎯 Main Throttling Tags
- `ThrottlingManager` - Core throttling logic and decisions
- `ThrottlingExt` - Extension function usage in ViewModels
- `P2PDataSource` - API calls with throttling integration

### 📱 Filtering Logs in Android Studio/Logcat

```
ThrottlingManager|ThrottlingExt|P2PDataSource
```

## Log Flow Sequence

### 1. 🔧 App Initialization
```
P2PDataSource: 🔧 P2PDataSourceImpl initialized - configuring throttling
P2PDataSource: ⚙️ configureThrottling() - setting up P2P operation throttling
P2PDataSource:    • Configuring P2P_GET_OFFERS: 10000ms interval
P2PDataSource:    • Configuring P2P_GET_OFFER_BY_ID: 5000ms interval
P2PDataSource:    • Configuring P2P_CREATE_OFFER: CREATE_OPERATIONS_CONFIG
...
ThrottlingManager: 🔧 configureOperation() - operationKey: 'P2P_GET_OFFERS'
ThrottlingManager:    • Config: intervalMs=10000, enabled=true
ThrottlingManager: ✅ Configuration saved for 'P2P_GET_OFFERS'
```

### 2. 🚀 API Call Initiated
```
P2PDataSource: 📋 getP2POffers() called with filters: P2PFilterRequest(...)
P2PDataSource: 🔍 Checking throttling for P2P_GET_OFFERS operation
```

### 3. 🔍 Throttling Decision Process
```
ThrottlingManager: 🔍 canExecute() - operationKey: 'P2P_GET_OFFERS'
ThrottlingManager: ⚙️ Config for 'P2P_GET_OFFERS': intervalMs=10000, enabled=true
ThrottlingManager: ⏱️ Time analysis for 'P2P_GET_OFFERS':
ThrottlingManager:    • Current time: 1234567890123
ThrottlingManager:    • Last execution: 1234567880000
ThrottlingManager:    • Time since last execution: 10123ms
ThrottlingManager: 📊 Interval throttling check for 'P2P_GET_OFFERS':
ThrottlingManager:    • Required interval: 10000ms
ThrottlingManager:    • Time since last execution: 10123ms
ThrottlingManager:    • Status: ALLOWED - sufficient time has passed
ThrottlingManager: ✅ 'P2P_GET_OFFERS' - ALLOWED to execute
```

### 4A. ✅ If Allowed (No Throttling)
```
P2PDataSource: ✅ Not throttled - proceeding immediately
P2PDataSource: 📝 Recording execution for P2P_GET_OFFERS
ThrottlingManager: 📝 recordExecution() - operationKey: 'P2P_GET_OFFERS'
ThrottlingManager:    • Execution time: 1234567890123
ThrottlingManager:    • Previous execution: 1234567880000
ThrottlingManager:    • Time between executions: 10123ms
ThrottlingManager: ✅ Execution recorded successfully for 'P2P_GET_OFFERS'
```

### 4B. ❌ If Blocked (Throttling Active)
```
ThrottlingManager:    • Status: BLOCKED - need to wait 3500ms more
ThrottlingManager: ❌ BLOCKED by interval throttling - Interval throttling: 10000ms required between executions
ThrottlingManager: ⏳ Remaining time: 3500ms (3.5s)
P2PDataSource: ⏸️ THROTTLED - waiting 3500ms before request
P2PDataSource:    • Reason: Interval throttling: 10000ms required between executions
[3.5 second delay]
P2PDataSource: ✅ Wait completed - proceeding with request
```

### 5. 🌐 HTTP Request Execution
```
P2PDataSource: 🌐 Preparing HTTP request
P2PDataSource:    • Access token provided: true
P2PDataSource:    • Full URL: https://api.qvapay.com/v1/p2p
P2PDataSource: ✅ HTTP request completed
P2PDataSource:    • Response status: 200 OK
P2PDataSource:    • Request duration: 250ms
P2PDataSource: ✅ Response parsing successful
P2PDataSource:    • Total offers: 45
P2PDataSource:    • Current page: 1
P2PDataSource:    • Offers in response: 15
P2PDataSource:    • Unique coins found: 8
```

## Key Metrics to Monitor

### ⏱️ Throttling Effectiveness
- **Wait times**: Look for "waiting Xms before request" logs
- **Execution intervals**: Monitor "Time between executions" values
- **Throttling frequency**: Count blocked vs allowed requests

### 🚀 Performance Impact
- **Request durations**: Monitor "Request duration: Xms" logs
- **Wait effectiveness**: Verify wait times match configured intervals
- **API response times**: Check if throttling helps reduce 429 errors

### 🐛 Troubleshooting
- **Configuration issues**: Look for config logs during app startup
- **Unexpected throttling**: Check time calculations and interval settings
- **API failures**: Monitor exception logs with throttling context

## Example Filter Commands

### Android Studio Logcat Filters
```bash
# All throttling logs
tag:ThrottlingManager | tag:ThrottlingExt | tag:P2PDataSource

# Only throttling decisions
tag:ThrottlingManager & text:"canExecute"

# Only blocked requests
tag:ThrottlingManager & text:"BLOCKED"

# Only API timings
tag:P2PDataSource & text:"duration"
```

### ADB Logcat Commands
```bash
# Real-time throttling monitoring
adb logcat -v time | grep -E "ThrottlingManager|ThrottlingExt|P2PDataSource"

# Filter for throttling decisions only
adb logcat -v time | grep -E "canExecute|THROTTLED|ALLOWED"

# Monitor wait times
adb logcat -v time | grep -E "waiting.*ms|Wait completed"
```

## Configuration Values to Verify

### Current P2P Operation Intervals
- **P2P_GET_OFFERS**: 10000ms (10 seconds)
- **P2P_GET_OFFER_BY_ID**: 5000ms (5 seconds) 
- **P2P_CREATE_OFFER**: CREATE_OPERATIONS_CONFIG (10 seconds)
- **P2P_APPLY_TO_OFFER**: CREATE_OPERATIONS_CONFIG (10 seconds)
- **P2P_CANCEL_OFFER**: 5000ms (5 seconds)
- **P2P_GET_MY_OFFERS**: 3000ms (3 seconds)

## Success Indicators

### ✅ Healthy Throttling System
1. Configuration logs appear during app startup
2. Subsequent API calls respect configured intervals
3. Wait times match expected throttling values
4. HTTP requests complete successfully after throttling
5. No excessive 429 "Too Many Requests" errors

### ❌ Issues to Watch For
1. Missing configuration logs (throttling not initialized)
2. Throttling wait times that don't match configs
3. Frequent exception logs during throttling
4. API calls bypassing throttling system
5. Persistent 429 errors despite throttling

---

**Note**: All logs use emojis for easy visual scanning. Filter by specific emojis or patterns to quickly identify relevant log entries during debugging.