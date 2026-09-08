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



## Phase 3 — Billing / Fee Configuration
Implemented:
- FeeComponent (ACTIVE/INACTIVE, immutable, no amount/frequency)
- FeeStructure (ACTIVE/INACTIVE, immutable)
- FeeStructureItem (component + frequency + amount)
- GradeFeeStructure (historical Grade -> FeeStructure assignment with effective dates)
- Active-only component/structure selection rules
- Responsive Thymeleaf/Bootstrap UI
- DTO-based REST APIs






# School Fee Management — Phase 4 Billing

Phase 4 builds Billing on the working Phase 3 Fee Configuration baseline.

## Scope
- System-wide single active Academic Year. Admin can create multiple years; creating/activating one deactivates the others.
- Billing uses the system-active Academic Year; users do not select an academic year for invoice generation/search.
- Invoice generation for all approved enrollments or one grade.
- Fee frequencies: ONE_TIME, YEARLY, HALF_YEARLY, QUARTERLY, MONTHLY.
- Monthly invoice items use the selected billing month for reporting; other frequencies use invoice generation month for reporting.
- InvoiceItem historical snapshots: fee component name, frequency, original amount, discount amount, net amount.
- Issued invoice search by admission number or mobile number.
- Staff/Admin can request discounts or invoice cancellation.
- Admin approval workflow for discounts and cancellations.
- Approved discounts update InvoiceItem discount/net amounts and create Discount facts.
- Approved cancellation changes Invoice status to CANCELLED.
- No Payment, PaymentAllocation, partial-payment or advance-payment functionality in Phase 4; those belong to Phase 5.

## UI
Responsive Bootstrap 5 + Bootstrap Icons + Thymeleaf Layout Dialect.

## Initial invoice generation
Generation is based on the active GradeFeeStructure assignment. Only ACTIVE FeeStructures can generate new invoices. Historical assignments are retained.

## Build
Run with JDK 17 and Maven:

    mvn clean test
    mvn spring-boot:run

Default identity users are inherited from Phase 1.

# Phase 5 Enhancements

Baseline: fee-mgr-v2-phase5-invoice-lazy-fix-v2.zip

Implemented:

1. Enrollments page
   - ADMIN and STAFF can filter enrollments by Grade.
   - Grade filter contains the application's current Grades.
   - Pagination preserves the selected Grade.

2. Grade Fee Structures
   - ADMIN and STAFF can view the current Grade -> Fee Structure links.
   - ADMIN can change the linked Fee Structure inline using a dropdown.
   - Only ACTIVE FeeStructures are offered in the dropdown.
   - STAFF is view-only on this page.
   - Changes create a new effective assignment for future invoice generation; existing invoices are unchanged.
   - Existing historical assignment view is retained.

3. Invoice lifecycle and request audit
   - Invoice statuses: DRAFT, ISSUED, PAID, CANCELLED.
   - Creating a DiscountRequest moves ISSUED -> DRAFT.
   - Creating a CancellationRequest moves ISSUED -> DRAFT.
   - DRAFT blocks both new request types and payment.
   - A DiscountRequest is allowed only once during an Invoice lifetime.
   - CancellationRequests can be created repeatedly, but only while the Invoice is ISSUED; a pending request must first be resolved.
   - Approved DiscountRequest: DRAFT -> ISSUED and discount is applied.
   - Rejected DiscountRequest: DRAFT -> ISSUED.
   - Approved CancellationRequest: DRAFT -> CANCELLED.
   - Rejected CancellationRequest: DRAFT -> ISSUED.
   - Payment is accepted only for ISSUED invoices and must equal Invoice net amount; successful payment changes ISSUED -> PAID.
   - PAID/CANCELLED invoices cannot be modified or paid.
   - Invoice Details displays full DiscountRequest audit and the latest CancellationRequest audit.
   - Reporting excludes DRAFT invoices from finalized financial totals and treats ISSUED/PAID as reportable.
   - Invoice generation treats DRAFT/ISSUED/PAID invoices as blocking duplicates, while CANCELLED invoices can be regenerated under the existing generation rules.
