import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import './App.css';
import Dashboard from './pages/Dashboard';
import CheckIn from './pages/CheckIn';
import CheckOut from './pages/CheckOut';
import AttendanceReport from './pages/AttendanceReport';
import WasteTasks from './pages/WasteTasks';
import Workers from './pages/Workers';

function App() {
  return (
    <Router future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
      <div className="App">
        <nav className="navbar">
          <div className="nav-container">
            <Link to="/" className="nav-logo">
              <span className="logo-icon">📊</span> Attendance System
            </Link>
            <ul className="nav-menu">
              <li className="nav-item">
                <Link to="/" className="nav-link">Dashboard</Link>
              </li>
              <li className="nav-item">
                <Link to="/checkin" className="nav-link">Check In</Link>
              </li>
              <li className="nav-item">
                <Link to="/checkout" className="nav-link">Check Out</Link>
              </li>
              <li className="nav-item">
                <Link to="/report" className="nav-link">Report</Link>
              </li>
              <li className="nav-item">
                <Link to="/tasks" className="nav-link">Tasks</Link>
              </li>
              <li className="nav-item">
                <Link to="/workers" className="nav-link">Workers</Link>
              </li>
            </ul>
          </div>
        </nav>

        <main className="main-content">
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/checkin" element={<CheckIn />} />
            <Route path="/checkout" element={<CheckOut />} />
            <Route path="/report" element={<AttendanceReport />} />
            <Route path="/tasks" element={<WasteTasks />} />
            <Route path="/workers" element={<Workers />} />
          </Routes>
        </main>

        <footer className="footer">
          <p>&copy; 2024 Waste Management Attendance System. All rights reserved.</p>
        </footer>
      </div>
    </Router>
  );
}

export default App;
