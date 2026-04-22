# 🔧 Network Error Troubleshooting - Complete Guide

## 🚨 Common Network Errors

### Error 1: AxiosError - Network Error (ERR_CONNECTION_REFUSED)

**Symptoms:**
```
AxiosError: Network Error
GET http://localhost:8080/api/attendance/today net::ERR_CONNECTION_REFUSED
```

**Root Cause:** Backend server is not running

**Quick Fix:**
```bash
# Terminal 1 - Start Backend
cd "Attendance-System"

# Option A: Using Docker
docker-compose up -d

# Option B: Using Java (requires Java 11+)
java -jar target/attendance-system-face-recognition-1.0.0.jar

# Wait 5-10 seconds for startup
```

**Verification:**
```bash
# Should return JSON (not error)
curl http://localhost:8080/api/workers
```

---

### Error 2: ERR_NETWORK_CHANGED

**Symptoms:**
```
GET http://localhost:8080/api/... net::ERR_NETWORK_CHANGED
```

**Cause:** Network connectivity issue or server restarted

**Fix:**
```bash
# 1. Check if backend is still running
curl http://localhost:8080/api/workers

# 2. If not, restart backend
docker-compose restart
# OR
java -jar target/attendance-system-face-recognition-1.0.0.jar

# 3. Hard refresh browser
Cmd+Shift+R (Mac) or Ctrl+Shift+R (Windows/Linux)
```

---

### Error 3: CORS Error

**Symptoms:**
```
Access to XMLHttpRequest at 'http://localhost:8080/api/...' 
from origin 'http://localhost:3000' has been blocked by CORS policy
```

**Cause:** Backend CORS configuration doesn't allow frontend origin

**Fix:**
Check `src/main/java/com/waste/management/config/WebConfig.java`:

```java
@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
            .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(false)
            .maxAge(3600);
}
```

If not configured, rebuild backend:
```bash
mvn clean package -DskipTests
docker-compose down && docker-compose up -d
# OR
java -jar target/attendance-system-face-recognition-1.0.0.jar
```

---

### Error 4: 404 Not Found

**Symptoms:**
```
GET http://localhost:8080/api/workers 404 (Not Found)
```

**Cause:** API endpoint doesn't exist or wrong path

**Fix:**
1. Check endpoint exists in backend
2. Check API URL in `frontend/src/services/api.js`:

```javascript
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';
```

Should match:
- Frontend: `http://localhost:3000`
- Backend: `http://localhost:8080`

---

### Error 5: 500 Internal Server Error

**Symptoms:**
```
POST http://localhost:8080/api/workers/register 500 (Internal Server Error)
```

**Cause:** Backend error during processing

**Debug Steps:**
```bash
# 1. Check backend logs
tail -100 logs/waste-management.log

# 2. Search for specific error
grep -i "error\|exception" logs/waste-management.log | tail -20

# 3. If using Docker
docker logs waste-management-app
```

**Common Fixes:**
- Invalid data format (check phone/aadhar numbers)
- Database connection issue
- Missing required fields
- Duplicate entries (email, aadhar, employee ID)

---

## 📋 Diagnostic Checklist

### Step 1: Verify Backend is Running
```bash
# Should return process information
lsof -i :8080

# Should return JSON (not error)
curl http://localhost:8080/api/workers

# Should show status 200
curl -I http://localhost:8080/api/workers
```

### Step 2: Verify Frontend is Running
```bash
# Should return HTML
curl http://localhost:3000

# Should show status 200
curl -I http://localhost:3000
```

### Step 3: Check Network Connectivity
```bash
# Test backend accessibility from frontend
curl http://localhost:8080/api/attendance/today

# Test CORS headers
curl -I -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: GET" \
  http://localhost:8080/api/workers
```

### Step 4: Check Browser Console
1. Open browser DevTools (F12)
2. Go to **Console** tab
3. Look for detailed error messages
4. Check **Network** tab for failed requests

### Step 5: Check Backend Logs
```bash
# View recent errors
tail -50 logs/waste-management.log

# Search for specific error
grep -i "ERROR" logs/waste-management.log | tail -20
```

---

## 🔄 Complete Restart Procedure

If all else fails, do a complete restart:

### Step 1: Stop All Services
```bash
# Stop Docker services
docker-compose down

# Kill any Java processes
killall java 2>/dev/null || true

# Kill Node processes
killall node 2>/dev/null || true
```

### Step 2: Clean Build
```bash
cd "Attendance-System"

# Clean build backend
mvn clean install -DskipTests

# Clean rebuild frontend
cd frontend
rm -rf node_modules package-lock.json build
npm install
npm run build
cd ..
```

### Step 3: Fresh Start

**Terminal 1 - Backend:**
```bash
cd "Attendance-System"

# Option A: Docker
docker-compose up -d
sleep 30
docker ps  # Should show 2 services: mysql and app

# Option B: Java
java -jar target/attendance-system-face-recognition-1.0.0.jar
```

**Terminal 2 - Frontend:**
```bash
cd "Attendance-System/frontend"
npm start
```

**Browser:**
- Clear cache: Cmd+Shift+Delete (Mac) or Ctrl+Shift+Delete (Windows)
- Hard refresh: Cmd+Shift+R (Mac) or Ctrl+Shift+R (Windows)
- Open: http://localhost:3000

---

## 🎯 Verification Commands

### Backend Endpoints
```bash
# Workers
curl http://localhost:8080/api/workers

# Attendance Today
curl http://localhost:8080/api/attendance/today

# Pending Tasks
curl http://localhost:8080/api/waste-tasks/pending

# Health Check (if available)
curl http://localhost:8080/api/recognition/health
```

### Port Availability
```bash
# Check if ports are in use
lsof -i :8080     # Backend
lsof -i :3000     # Frontend
lsof -i :3306     # Database (if Docker)

# Check specific port status
netstat -an | grep 8080
```

### Service Status
```bash
# Docker services
docker ps -a
docker-compose ps

# Running processes
ps aux | grep java
ps aux | grep node

# Port usage
netstat -tlnp | grep LISTEN
```

---

## 📱 Frontend API Configuration

If you need to point frontend to different backend:

**File:** `frontend/.env` (create if doesn't exist)

```
REACT_APP_API_URL=http://localhost:8080/api
```

Then rebuild:
```bash
npm run build
```

---

## 🚀 Deployment Checklist

Before deploying to production:

- [ ] Backend builds successfully: `mvn clean package`
- [ ] Frontend builds successfully: `npm run build`
- [ ] All API endpoints tested and working
- [ ] CORS is properly configured
- [ ] Database is initialized
- [ ] Logs are being written
- [ ] Upload directories exist (`uploads/`, `data/faces/`)
- [ ] No hard-coded localhost URLs
- [ ] Error handling is comprehensive
- [ ] All services restart correctly

---

## 📞 Getting Help

If you're still getting errors:

1. **Share the exact error message**
2. **Share backend logs:**
   ```bash
   tail -100 logs/waste-management.log
   ```
3. **Share frontend console output** (F12 → Console)
4. **Share network requests** (F12 → Network tab)
5. **Verify both services are running:**
   ```bash
   curl http://localhost:8080/api/workers
   curl http://localhost:3000
   ```

