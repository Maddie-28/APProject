# APProject
# University ERP System

A robust, Java Swing-based desktop application designed to manage university operations including course scheduling, student enrollment, grading, and system administration.

This project utilizes a **Cloud-based Database (TiDB)** for storage and implements strict role-based access control (RBAC) with high security standards.

---

## 🚀 Key Features

### 🔐 Security & Authentication
* **Two-Database Architecture:** Separates Authentication data (`users_auth`) from ERP data for security.
* **Password Hashing:** Uses **BCrypt** to hash passwords (UNIX "shadow" style). Real passwords are never stored.
* **Account Lockout:** Automatically locks accounts for 5 minutes after 5 failed login attempts.
* **Audit Logging:** Tracks all login attempts (success/fail) and admin actions in `system_logs`.

### 🎓 Student Module
* **Course Catalog:** View courses with real-time "Seats Available" and "Status" (Open/Full/Closed).
* **Registration Rules:** Validates **Capacity** and **Registration Deadlines** before enrolling.
* **Timetable:** Visualizes the weekly schedule based on enrolled sections.
* **Transcript:** View grades and download an official CSV transcript.

### 👨‍🏫 Instructor Module
* **Section Management:** View only assigned sections (Security check enforced).
* **Grading:** Enter scores for Quiz, Midterm, and End-Sem.
* **Auto-Calculation:** Computes Final Grade based on the **20/30/50 weighted rule** and assigns Letter Grades (A-F).
* **Statistics:** View min/max/average scores for the class.

### 🛠 Admin Module
* **User Management:** Create Students and Instructors (auto-generates Auth & Profile records).
* **Course Scheduling:** Create Sections with specific **Registration & Drop Deadlines**.
* **Maintenance Mode:** A global "Kill Switch" that makes the system Read-Only for all users.
* **System Logs:** View security events and user activity.

---

## 🛠 Tech Stack

* **Language:** Java 17
* **UI Framework:** Swing + **FlatLaf** (Modern Look & Feel)
* **Database:** TiDB Cloud (MySQL Compatible) via JDBC
* **Security:** JBCrypt (Password Hashing)
* **Testing:** JUnit 5 + Mockito
* **Build Tool:** Standard Java Project (Dependencies in `/lib`)

---

## ⚙️ Setup & How to Run

**Prerequisites:**
* Java Development Kit (JDK) 17 or higher.
* Active Internet Connection (Required to connect to the Cloud Database).

**Steps:**
1.  **Open the Project:** Open the folder in IntelliJ IDEA or Eclipse.
2.  **Verify Libraries:** Ensure the JARs in the `lib` folder (mysql-connector, flatlaf, jbcrypt) are added to the build path.
3.  **Run the App:**
    * Navigate to `src/edu/univ/erp/Main.java`.
    * Right-click -> **Run 'Main'**.
4.  **Database Setup:**
    * *No local setup required.* The app connects to a pre-configured remote TiDB instance.
    * To reset data, run `src/edu/univ/erp/DatabaseSeeder.java`.

---

## 🔑 Default Credentials (Sample Data)

Use these accounts to test the different roles:

| Role | Username | Password | Notes |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin` | `admin123` | Full access. Can toggle maintenance. |
| **Instructor** | `inst1` | `pass123` | Teaches CS101. |
| **Student** | `student1` | `pass123` | Already enrolled in CS101. |
| **Student** | `student2` | `pass123` | New student (empty schedule). |

---

## 🧪 Testing

The project includes a robust **JUnit 5** test suite located in `src/edu/univ/erp/service`.

* **`StudentServiceTest`:** Verifies deadlines, capacity checks, and duplicate prevention.
* **`AdminServiceTest`:** Verifies user creation and delete protection (cannot delete sections with students).
* **`InstructorServiceTest`:** Verifies grading logic and security (cannot grade unassigned sections).

To run tests: Right-click the `service` package -> **Run 'All Tests'**.

---

## 📊 Database Schema

The system uses two schemas for security separation:

1.  **Auth DB:** `users_auth` (id, username, password_hash, role, lockout_status)
2.  **ERP DB:**
    * `students`, `instructors` (Profiles linked by user_id)
    * `courses`, `sections` (Academic data with Deadlines)
    * `enrollments` (Links students to sections)
    * `grades` (Stores component scores)
    * `settings` (Global flags like Maintenance Mode)
    * `system_logs` (Audit trail)

---

## 📝 Business Rules Implemented

1.  **Grading Logic:**
    * Quiz: 20%
    * Midterm: 30%
    * End-Sem: 50%
2.  **Maintenance Mode:**
    * When ON, Students cannot Register/Drop.
    * Instructors cannot Enter Grades/Compute Finals.
    * Admins retain full access.
3.  **Delete Protection:**
    * A Section cannot be deleted by an Admin if students are currently enrolled.

---

**Developed by:** Krishna and Madhvesh
**Date:** November 2025