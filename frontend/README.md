# Attendance System Frontend

React-based frontend for the Waste Management Attendance System with Face Recognition.

## Features

- Employee Check-In/Check-Out with face recognition
- Attendance Reports with date range filtering
- Waste Management Task Management
- Worker Directory
- Real-time Dashboard with statistics

## Setup Instructions

### Prerequisites
- Node.js 14+ and npm

### Installation

1. Navigate to the frontend directory:
```bash
cd frontend
```

2. Install dependencies:
```bash
npm install
```

3. Create `.env` file (copy from `.env.example`):
```bash
cp .env.example .env
```

4. Update `.env` with your backend API URL:
```
REACT_APP_API_URL=http://localhost:8080/api
```

### Running the Application

Start the development server:
```bash
npm start
```

The application will open at `http://localhost:3000`

### Building for Production

```bash
npm run build
```

This creates an optimized production build in the `build` folder.

## Project Structure

```
frontend/
├── public/
│   └── index.html
├── src/
│   ├── components/
│   ├── pages/
│   │   ├── Dashboard.jsx
│   │   ├── CheckIn.jsx
│   │   ├── CheckOut.jsx
│   │   ├── AttendanceReport.jsx
│   │   ├── WasteTasks.jsx
│   │   └── Workers.jsx
│   ├── services/
│   │   └── api.js
│   ├── styles/
│   │   ├── Dashboard.css
│   │   ├── Forms.css
│   │   └── Tables.css
│   ├── App.jsx
│   ├── App.css
│   ├── index.js
│   └── index.css
├── package.json
└── README.md
```

## API Integration

The frontend communicates with the backend via REST API endpoints. All API calls are centralized in `src/services/api.js`.

### Available Endpoints

- **Attendance**: Check-in, Check-out, Reports
- **Workers**: Register, List, Update
- **Waste Tasks**: Create, Assign, Complete, Cancel

## Components

### Dashboard
Real-time statistics showing:
- Present/Absent employees
- Attendance rate
- Pending tasks
- In-progress tasks

### Check-In/Check-Out
Forms for employee attendance with:
- Employee ID input
- Image capture for face recognition
- GPS location tracking

### Reports
Attendance history with:
- Date range filtering
- Employee details
- Face match confidence scores

### Tasks Management
Waste task tracking with:
- Task status filtering
- Worker assignments
- Driver assignments

### Workers Directory
List of all active workers with:
- Role information
- Contact details
- Department assignment
- Status tracking
