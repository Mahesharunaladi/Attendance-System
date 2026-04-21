-- Create waste_management database
CREATE DATABASE IF NOT EXISTS waste_management;
USE waste_management;

-- Workers table
CREATE TABLE IF NOT EXISTS workers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id VARCHAR(50) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone_number VARCHAR(20),
    role ENUM('CLEANER', 'DRIVER', 'HELPER', 'SUPERVISOR', 'MANAGER') NOT NULL,
    facial_data_path VARCHAR(255) NOT NULL,
    department VARCHAR(100),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_employee_id (employee_id),
    INDEX idx_role (role),
    INDEX idx_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Attendance Records table
CREATE TABLE IF NOT EXISTS attendance_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    worker_id BIGINT NOT NULL,
    check_in_time TIMESTAMP NOT NULL,
    check_out_time TIMESTAMP,
    status ENUM('PRESENT', 'ABSENT', 'LATE', 'EARLY_LEAVE', 'ON_DUTY') NOT NULL,
    face_match_confidence DOUBLE NOT NULL,
    location_latitude DOUBLE,
    location_longitude DOUBLE,
    notes VARCHAR(500),
    image_capture_path VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (worker_id) REFERENCES workers(id) ON DELETE CASCADE,
    INDEX idx_worker_id (worker_id),
    INDEX idx_check_in_time (check_in_time),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Waste Tasks table
CREATE TABLE IF NOT EXISTS waste_tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id VARCHAR(50) NOT NULL UNIQUE,
    area VARCHAR(255) NOT NULL,
    waste_type VARCHAR(100) NOT NULL,
    estimated_weight DOUBLE NOT NULL,
    actual_weight DOUBLE DEFAULT 0,
    status ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'ON_HOLD') NOT NULL,
    assigned_to_worker_id BIGINT,
    driver_id BIGINT,
    scheduled_date TIMESTAMP NOT NULL,
    completion_date TIMESTAMP,
    location_latitude DOUBLE,
    location_longitude DOUBLE,
    notes VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (assigned_to_worker_id) REFERENCES workers(id) ON DELETE SET NULL,
    FOREIGN KEY (driver_id) REFERENCES workers(id) ON DELETE SET NULL,
    INDEX idx_task_id (task_id),
    INDEX idx_status (status),
    INDEX idx_assigned_to_worker_id (assigned_to_worker_id),
    INDEX idx_driver_id (driver_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create indexes for better query performance
CREATE INDEX idx_attendance_date ON attendance_records(DATE(check_in_time));
CREATE INDEX idx_waste_task_date ON waste_tasks(DATE(scheduled_date));

-- Insert sample data
INSERT INTO workers (employee_id, full_name, email, phone_number, role, facial_data_path, department, active) VALUES
('EMP001', 'Rajesh Kumar', 'rajesh@waste.com', '9876543210', 'CLEANER', 'data/faces/rajesh.jpg', 'Waste Management', 1),
('EMP002', 'Arun Singh', 'arun@waste.com', '9876543211', 'CLEANER', 'data/faces/arun.jpg', 'Waste Management', 1),
('EMP003', 'Priya Sharma', 'priya@waste.com', '9876543212', 'HELPER', 'data/faces/priya.jpg', 'Waste Management', 1),
('EMP004', 'Vikram Patel', 'vikram@waste.com', '9876543213', 'DRIVER', 'data/faces/vikram.jpg', 'Transportation', 1),
('EMP005', 'Neha Desai', 'neha@waste.com', '9876543214', 'SUPERVISOR', 'data/faces/neha.jpg', 'Management', 1);

COMMIT;
