# LifeLink System Guide

This guide describes the current LifeLink application, how to configure and run it locally, and how its modules use the shared MySQL database. LifeLink uses server-rendered Spring MVC pages; users should sign in with their assigned account and role before opening restricted modules.

## Purpose and features

LifeLink supports a donor journey from account registration through preliminary health screening, appointment requests, donation history, and blood-request tracking. Administrators manage donor records, assessment reviews, appointments, donations, inventory, requests, and system reports.

Current modules include:

- Public Home, About, Features, and Contact pages.
- Donor and administrator registration and sign-in, session-based role routing, and logout.
- Donor dashboard, profile, eligibility form, scheduling, history, and blood requests.
- Administrator dashboard, donor management, assessment review, appointment management, donation entry, request management, inventory, and Reports.
- Reports for donors (including archived status), donations, eligibility assessments, appointments, and inventory. Each applicable report has its own search and filters; date filters apply to dated records.

Eligibility outcomes are preliminary screening results. They do not replace a medical decision by authorized clinical staff.

## Technology and architecture

| Layer | Implementation |
| --- | --- |
| Web framework | Java 17+, Spring Boot 3.5.4, Spring MVC |
| Views | Thymeleaf templates in `src/main/resources/templates` |
| Browser assets | HTML, CSS, and JavaScript in `src/main/resources/static` |
| Persistence | Spring Data JPA and Hibernate |
| Database | MySQL-compatible database through MySQL Connector/J |
| Tests | JUnit 5, Spring Boot Test, and MockMvc |

Controllers in `lifelink.controller` handle routes and sessions. `EligibilityService` evaluates preliminary screening according to the current business rules. Spring Data repositories in `lifelink.repository` and `lifelink.service` load and persist entities. Thymeleaf renders server-side HTML; browser JavaScript enhances navigation, password visibility, and client-side report filtering.

## Roles and access

| Role | Main access |
| --- | --- |
| Donor | Donor dashboard and personal donor modules |
| Admin | Admin dashboard, donor and operational management, and Reports |
| Public visitor | Public information pages, registration, and login |

Controllers check the session role before rendering protected pages. Successful login stores the user ID, username, display name, and role in the HTTP session, then redirects by role. Logout invalidates the session.

## Database structure

The application maps the following tables through its JPA entities. Hibernate currently uses `ddl-auto=update`, so it may update schema definitions when the application starts; it does not issue a database reset.

| Table / entity | Purpose and important relationships |
| --- | --- |
| `users` / `User` | Login identity, email, first and last name, role, and password hash. A donor profile may link one-to-one to a user. |
| `donors` / `Donor` | Donor contact details, age, gender, blood type, archive flag, and optional user link. |
| `eligibility_assessments` / `EligibilityAssessment` | Answers, readiness score, result status, reasons, next eligible date, submission time, and required donor reference. |
| `donation_records` / `DonationRecord` | Donation date, type, bag count, status, inventory-applied flag, note, and required donor reference. |
| `appointments` / `Appointment` | Hospital, city, appointment time, status, admin note, reschedule reference, creation time, and required donor reference. |
| `blood_requests` / `BloodRequest` | Requester and patient details, urgency, status, authorization flags, and optional supporting document. Requests link to a user. |
| `blood_inventory` / `BloodInventory` | Unique blood type and available bag count. |

Relationships are represented by JPA foreign keys. Donation and assessment records reference a donor; a blood request references its requester user. Completing a donation updates inventory once using the donation record's `inventoryApplied` flag. Receiving an approved blood request deducts stock once using its `inventoryDeducted` flag.

The current schema does not store donor registration timestamps. The donor report therefore uses donor IDs and available profile fields rather than displaying an invented registration date.

## Local installation and configuration

### Requirements

- JDK 17 or newer.
- MySQL or a compatible local MySQL service.
- The Maven wrapper included in this repository.

### Database setup

Create the database if it does not already exist:

```sql
CREATE DATABASE IF NOT EXISTS lifelink_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Do not drop or recreate an existing database to run the application. JPA entities and `spring.jpa.hibernate.ddl-auto=update` maintain the development schema.

### Connection settings

The defaults in `src/main/resources/application.properties` are:

- URL: `jdbc:mysql://localhost:3306/lifelink_db`
- Username: `root`
- Password: empty
- HTTP port: `8080`

Override the database settings with `LIFELINK_DB_URL`, `LIFELINK_DB_USERNAME`, and `LIFELINK_DB_PASSWORD`. For example, in PowerShell:

```powershell
$env:LIFELINK_DB_URL = 'jdbc:mysql://localhost:3306/lifelink_db'
$env:LIFELINK_DB_USERNAME = 'root'
$env:LIFELINK_DB_PASSWORD = 'your-local-password'
```

Set credentials for the local database you control. Do not commit credentials to source control.

### Start and test

From the repository root:

```powershell
.\mvnw.cmd spring-boot:run
```

Open `http://localhost:8080/`. To run automated tests:

```powershell
.\mvnw.cmd test
```

The project can also be packaged with `.mvnw.cmd package`.

## Reports

Open **Reports** from the Admin navigation or visit `/admin/reports` while signed in as an admin. The report controller loads data through the existing donor, donation, eligibility, appointment, and inventory repositories. It does not use sample records.

- Donor report: donor ID, name, username where linked, email, phone, blood type, gender, and active/archive status. Search and blood-type/status filters work together.
- Donation report: donation ID, date, donor, blood type, bag count, and status. Search, blood type, status, and date range work together.
- Eligibility report: assessment ID, submission date, donor, blood type, readiness score, result, next eligible date, and recorded reasons. Search, blood type, result, and date range work together.
- Appointment report: appointment ID, time, donor, blood type, center, city, and status. Search, blood type, status, and date range work together.
- Inventory report: blood type, bag count, and a low-stock indicator at ten bags or fewer.

Each report section filters only its own rows and presents a no-record or no-match message. A date range with a start date after its end date shows an inline validation message. Reports are protected by the existing Admin role check.

## Demo sequence and backup plan

See [Demo Runbook](Demo-Runbook.md) for an ordered walk-through. Use sanitized test records only. Before a live demonstration, confirm the database is available, sign in with prepared accounts, and open each page in sequence. If the database or app host is unavailable, use the architecture and module slides, explain that live data could not be refreshed, and do not present sample values as database output.

## Known limitations and next steps

- Donor registration time is not captured by the current model, so it cannot be included in the donor report without a schema change and migration plan.
- Report filters currently run in the browser over the rows rendered by the server. Large deployments should use validated server-side pagination and filtering.
- `ddl-auto=update` is retained only for local development. The `prod` profile validates schema without modifying it; production schema changes must use reviewed migrations.
- Admin self-registration has no source-code fallback. It stays disabled until `LIFELINK_ADMIN_REGISTRATION_CODE` is supplied through a protected environment setting.
- The custom session-role authorization does not yet include CSRF protection. Do not expose the application publicly until CSRF coverage is implemented and verified.
- Assessment output is a preliminary screening aid; authorized medical staff determine clinical eligibility.
- Automated browser tests for responsive layouts and interactive report filtering remain future work.
