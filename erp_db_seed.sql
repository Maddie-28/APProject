CREATE DATABASE IF NOT EXISTS erp_db;
USE erp_db;

CREATE TABLE IF NOT EXISTS instructors (
    user_id INT PRIMARY KEY,
    department VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS students (
    user_id INT PRIMARY KEY,
    roll_no VARCHAR(20) NOT NULL UNIQUE,
    program VARCHAR(100) NOT NULL,
    year INT NOT NULL
);

CREATE TABLE IF NOT EXISTS courses (
    course_code VARCHAR(10) PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    credits INT NOT NULL
);

CREATE TABLE IF NOT EXISTS sections (
    section_id INT PRIMARY KEY AUTO_INCREMENT,
    course_code VARCHAR(10) NOT NULL,
    instructor_id INT NULL,
    day_time VARCHAR(50) NOT NULL,
    room VARCHAR(20) NOT NULL,
    capacity INT NOT NULL,
    semester VARCHAR(20) NOT NULL,
    year INT NOT NULL,
    FOREIGN KEY (course_code) REFERENCES courses(course_code),
    FOREIGN KEY (instructor_id) REFERENCES instructors(user_id)
);

CREATE TABLE IF NOT EXISTS enrollments (
    enrollment_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    section_id INT NOT NULL,
    status ENUM('enrolled', 'dropped', 'completed') NOT NULL DEFAULT 'enrolled',
    final_grade VARCHAR(2) NULL,
    FOREIGN KEY (student_id) REFERENCES students(user_id),
    FOREIGN KEY (section_id) REFERENCES sections(section_id),
    UNIQUE (student_id, section_id)
);

CREATE TABLE IF NOT EXISTS grades (
    grade_id INT PRIMARY KEY AUTO_INCREMENT,
    enrollment_id INT NOT NULL,
    component VARCHAR(50) NOT NULL,
    score DECIMAL(5, 2) NOT NULL,
    FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS settings (
    setting_key VARCHAR(50) PRIMARY KEY,
    setting_value VARCHAR(255) NOT NULL
);

INSERT IGNORE INTO settings (setting_key, setting_value)
VALUES ('maintenance_on', 'false');

CREATE TABLE IF NOT EXISTS system_logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    username VARCHAR(50),
    action VARCHAR(50),       -- e.g., "LOGIN", "CREATE_USER", "DROP_SECTION"
    details VARCHAR(255),     -- e.g., "Success", "Failed: Wrong Password", "Created CS101"
    ip_address VARCHAR(50),   -- Optional, good for security
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE sections
    ADD COLUMN reg_deadline DATE NULL,
    ADD COLUMN drop_deadline DATE NULL;
