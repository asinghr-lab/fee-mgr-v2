# School Fee Management — Phase 1

This baseline implements **Module 1: Identity**.

Package convention for every module:
- `com.discover.app.<module-name>.domain`
- `com.discover.app.<module-name>.repository`
- `com.discover.app.<module-name>.service`
- `com.discover.app.<module-name>.dto`
- `com.discover.app.<module-name>.web`
- `com.discover.app.<module-name>.api`

Identity entities:
- User
- UserRole
- UserProfile
- Role

Seed users:
- admin / admin123
- staff / staff123

Run:
`mvn clean test`
then:
`mvn spring-boot:run`

Login:
`http://localhost:8080/login`

REST:
`GET /api/identity/me`

The remaining modules will be added one at a time on top of this baseline.



# School Fee Management — Phase 2 (Identity + School)

This release uses the working Phase-1 Identity application as its baseline and adds the School module with a responsive Bootstrap/Thymeleaf UI.

## Technology
- Spring Boot 4.1.0
- JDK 17
- Maven
- H2
- Spring Data JPA
- Spring Security
- Thymeleaf + Thymeleaf Layout Dialect
- Bootstrap 5.3
- Bootstrap Icons
- JUnit 5 / Spring Boot Test dependencies

## Package convention
Each module follows:
- `com.discover.app.<module-name>.domain`
- `com.discover.app.<module-name>.repository`
- `com.discover.app.<module-name>.service`
- `com.discover.app.<module-name>.dto`
- `com.discover.app.<module-name>.web`
- `com.discover.app.<module-name>.api`

## Phase 2 School module
Entities:
- School
- AcademicYear
- Grade
- Student
- StudentEnrollment
- EnrollmentStatus: REQUESTED, APPROVED, CANCELLED

Workflow:
- ADMIN creates/edits School details.
- ADMIN creates Academic Years.
- ADMIN creates/edits Grades.
- ADMIN/STAFF adds Students.
- Student admissionDate is automatically set to the creation date.
- ADMIN/STAFF requests enrollment for a student, academic year and grade.
- ADMIN approves enrollment requests.
- Enrollment requests can be cancelled; CANCELLED is terminal. A new request must be created for re-enrollment.
- Enrollment captures requestedBy/requestDate and approvedBy/approvalDate.
- ADMIN can view active and historical enrollment requests.

## UI
All authenticated application pages use a reusable Thymeleaf Layout Dialect layout with:
- responsive Bootstrap 5 navigation
- mobile off-canvas sidebar
- Bootstrap Icons
- reusable sidebar fragment
- responsive tables/forms
- improved visual styling and accessible status badges

## Run
`mvn clean test`

`mvn spring-boot:run`

Open `http://localhost:8080/login`.

Seed users:
- admin / admin123
- staff / staff123
