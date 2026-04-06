# CivicPulseHub – Smart City Grievance Management System

CivicPulseHub is a modularized Spring Boot application for efficient civic grievance management, featuring role-based access control, real-time status tracking, and department-level issue resolution.

## System Modules

### 🔐 Module 1: Auth & User Management
- **Security**: JWT-based authentication with HS512 signing.
- **Roles**: Distinct permissions for `CITIZEN`, `OFFICER`, and `ADMIN`.
- **Services**: Secure registration and login flows with encrypted passwords.

### 📝 Module 2: Citizen Grievance Portal
- **Submission**: Citizens can file grievances with titles, descriptions, categories, and locations.
- **Image Support**: Base64 image uploads for visual evidence during submission.
- **Tracking**: Personal dashboard to view submitted issues and their current status.

### 🛡️ Module 3: Admin & Management Queue
- **Statistics**: Real-time stats dashboard for total, pending, and resolved grievances.
- **Assignments**: Admins can assign grievances to specific department officers.
- **Prioritization**: Set severity levels (Low, Medium, High, Critical) and deadlines.
- **Filtering**: Advanced filtering by category, status, priority, and search queries.

### 👷 Module 4: Department Officer Module
- **Task Management**: Officers have a dedicated view for grievances assigned specifically to them.
- **Resolution Flow**: Capability to mark progress ("In Progress") and add internal resolution notes.
- **Evidence of Resolution**: Upload completion imagery (proof) once a grievance is marked as "Resolved".

### ⭐ Module 5: Feedback & Rating System
- **Citizen Reviews**: Citizens can provide a 1-5 star rating and optional feedback once an issue is resolved.
- **Escalation & Reopen**: Option to "Reopen" a grievance if the citizen is unsatisfied with the resolution.
- **Interactive Star Rating**: Custom-built interactive rating component for an intuitive experience.
- **Responsive UI**: Entire application optimized for both mobile and desktop screens with centered grid layouts.

## Tech Stack
- **Backend**: Java 17, Spring Boot 3.3.4, Spring Security, JPA, Hibernate.
- **Database**: H2 (In-memory for development).
- **Frontend**: Vanilla HTML5, CSS3, and JavaScript (Integrated as Static Resources).
- **Build**: Gradle.

## Running Locally
1. Ensure your system has JDK 17 installed.
2. Run the application using Gradle:
   ```bash
   ./gradlew bootRun
   ```
3. Access the dashboard at `http://localhost:9090`.

---
*Built for the CivicPulse Internship Project*
