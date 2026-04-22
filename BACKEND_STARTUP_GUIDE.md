# 🚀 Backend Server Startup Guide - AxiosError Connection Refused Fix

## 🔴 Problem
```
AxiosError: Network Error
GET http://localhost:8080/api/attendance/today net::ERR_CONNECTION_REFUSED
```

**Cause:** Backend server is not running on port 8080

---

## ✅ Solution: Start Backend Server

### Option 1: Using Docker (Recommended - No Java Installation Required)

#### Prerequisites
- Docker Desktop installed and running

#### Steps
```bash
cd "/Users/mahesharunaladi/Documents/Attendance System/Attendance-System"

# Start backend and database
docker-compose up -d

# Wait 30 seconds for initialization
sleep 30

# Check if services are running
docker ps

# View logs
docker logs waste-management-app
```

#### Verify Backend is Running
```bash
curl http://localhost:8080/api/workers
```

Expected output:
```json
[]
```

---

### Option 2: Direct Java Execution (Requires Java Installation)

#### Prerequisites
- Java 11 or higher installed

#### Install Java on macOS
```bash
# Using Homebrew
brew install openjdk@11

# Link Java to system
sudo ln -sfn /usr/local/opt/openjdk@11/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-11.jdk
```

#### Start Backend
```bash
cd "/Users/mahesharunaladi/Documents/Attendance System/Attendance-System"

# Make script executable
chmod +x start-backend.sh

# Run the startup script
./start-backend.sh

# OR run directly
java -jar target/attendance-system-face-recognition-1.0.0.jar
```

---

## 🖥️ Starting Frontend (Separate Terminal)

Once backend is running:

```bash
# In a NEW terminal window
cd "/Users/mahesharunaladi/Documents/Attendance System/Attendance-System/frontend"

# Install dependencies (if not already done)
npm install

# Start React development server
npm start
```

This will open the application at: **http://localhost:3000**

---

## 🔍 Verification Checklist

### Backend Health Check
```bash
# Check if backend is running
curl http://localhost:8080/api/workers

# Check specific endpoint
curl http://localhost:8080/api/attendance/today

# If successful, you'll see JSON response (not error)
```

### Expected Status
- ✅ Backend: http://localhost:8080
- ✅ Frontend: http://localhost:3000
- ✅ Database: localhost:3306 (if using Docker)
- ✅ Logs: `logs/waste-management.log`

---

## 🐛 Troubleshooting

### Still Getting Connection Refused?

#### Check 1: Is backend running?
```bash
# List running processes
lsof -i :8080

# Expected output should show Java process
```

#### Check 2: Check logs for errors
```bash
# View last 50 lines of log
tail -50 logs/waste-management.log

# Search for errors
grep -i "error\|exception" logs/waste-management.log | tail -20
```

#### Check 3: Is port 8080 available?
```bash
# Kill any process using port 8080
lsof -ti:8080 | xargs kill -9

# Restart backend
```

#### Check 4: Check frontend API URL configuration
```bash
# Check api.js configuration
cat frontend/src/services/api.js | grep "API_BASE_URL"

# Should show: http://localhost:8080/api
```

#### Check 5: Docker-specific issues
```bash
# If using Docker, check container status
docker ps -a

# Check container logs
docker logs waste-management-app

# Restart services
docker-compose restart

# View service health
docker-compose ps
```

---

## 📋 Complete Startup Sequence

### Terminal 1 - Backend
```bash
cd "/Users/mahesharunaladi/Documents/Attendance System/Attendance-System"

# Option A: Docker (Recommended)
docker-compose up -d

# Option B: Direct Java
./start-backend.sh
# OR
java -jar target/attendance-system-face-recognition-1.0.0.jar

# Wait for: "Attendance System - Ready!"
```

### Terminal 2 - Frontend
```bash
cd "/Users/mahesharunaladi/Documents/Attendance System/Attendance-System/frontend"
npm start

# Wait for: "Compiled successfully"
```

### Browser
Open: **http://localhost:3000**

---

## ✨ Expected Behavior

Once everything is running:

1. ✅ Dashboard loads without errors
2. ✅ Workers list displays
3. ✅ Can navigate to Check-In page
4. ✅ Can navigate to Register page
5. ✅ Face recognition works
6. ✅ No "Connection Refused" errors

---

## 🎯 Quick Reference

| Task | Command |
|------|---------|
| Start backend (Docker) | `docker-compose up -d` |
| Start backend (Java) | `java -jar target/attendance-system-face-recognition-1.0.0.jar` |
| Start frontend | `npm start` |
| Stop backend (Docker) | `docker-compose down` |
| View backend logs | `tail -50 logs/waste-management.log` |
| Check port 8080 | `lsof -i :8080` |
| Kill process on port 8080 | `lsof -ti:8080 \| xargs kill -9` |
| Test backend API | `curl http://localhost:8080/api/workers` |

---

## 📞 Still Having Issues?

1. **Check both terminals are running** (Backend + Frontend)
2. **Check logs for specific errors** (see Troubleshooting section)
3. **Verify ports are correct** (8080 for backend, 3000 for frontend)
4. **Clear browser cache** (Ctrl+Shift+Delete or Cmd+Shift+Delete)
5. **Hard refresh browser** (Ctrl+Shift+R or Cmd+Shift+R)

