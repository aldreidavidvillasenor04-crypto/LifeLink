# LifeLink

LifeLink is a server-rendered Spring Boot 3.5 application using Java 17, Thymeleaf, Spring MVC, JPA, and MySQL. It includes public information pages, donor registration and dashboards, eligibility assessments, appointments, blood requests, inventory, donation records, and administrator reports.

## Run locally

Requirements: JDK 17+, the included Maven wrapper, and a local MySQL-compatible server.

1. Start MySQL and create the local database if needed:

   ```sql
   CREATE DATABASE IF NOT EXISTS lifelink_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. The local defaults are `jdbc:mysql://localhost:3306/lifelink_db`, user `root`, and an empty password. If your setup differs, set `LIFELINK_DB_URL`, `LIFELINK_DB_USERNAME`, and `LIFELINK_DB_PASSWORD` in your terminal.
3. From the project directory, run:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

4. Open `http://localhost:8080/`.

Run automated checks with:

```powershell
.\mvnw.cmd test
```

## Online deployment

The project has a Dockerfile and production Spring profile for Railway with a separate MySQL service. Public deployment is not complete: it requires your GitHub and Railway accounts, a hosted database, and a deliberate decision about whether any existing donor or patient records may be transferred.

Follow [Production Deployment](docs/Production-Deployment.md) for GitHub preparation, Railway setup, environment variables, the schema migration, data-transfer safeguards, and post-deploy verification.

Do not use real donor or patient data in a public deployment until the organization has approved the hosting region, access controls, privacy notice/consent, retention, backups, and applicable legal and clinical safeguards. Use synthetic accounts and data while testing.

## Project documentation

- [System guide](docs/LifeLink-System-Guide.md)
- [Test results](docs/Testing-Results.md)
- [Audit report](docs/Audit-Report-2026-10-28.md)
- [Demonstration runbook](docs/Demo-Runbook.md)
- [Final presentation](docs/presentation/LifeLink-Final-Presentation.pptx)
