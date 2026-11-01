# LifeLink audit report — 28 October 2026

Scope: current Java/Spring Boot/Thymeleaf/MySQL project, controllers, repositories, services, entities, views, static assets, test suite, database configuration, and deployment setup. The application structure was preserved. The project has no Git metadata, so a source archive was made before editing.

## A. Problems found

| Severity | Finding | Root cause / status |
| --- | --- | --- |
| High | Admin self-registration could be enabled with a fixed code embedded in source. | A public, reusable code was hardcoded in `RegistrationController`. Removed; admin signup now requires a configured secret and is hidden in the registration form when disabled. |
| High | Application is not ready for public production. | Custom session-role checks do not include CSRF middleware. Public state-changing forms therefore need CSRF protection before internet exposure. |
| High | Current local MySQL requests stalled. | MySQL process inspection showed a donor query in `Statistics` and table-open/count queries waiting for over 11 minutes. A fresh test run could not finish; the database was not reset or repaired. |
| High | Production deployment cannot be completed from this workspace. | No host, production database, domain/TLS endpoint, or deployment credentials are configured. Docker is also unavailable locally. |
| Medium | Incomplete donor profiles could reach routes that dereference a missing profile. | Only the dashboard handled that state. Other donor pages now redirect to profile completion. |
| Medium | Donation updates accepted arbitrary status values and could reverse a completed donation after stock was added. | Status was not validated and completed inventory changes were not treated as final. Inputs are now restricted; an inventory-applied donation cannot be moved away from `COMPLETED`. |
| Medium | Donor test rendered all current database donors. | The test called `findAll()` against live local data. Replaced with one transactional fixture to avoid a full-record scan and prevent real donor data from being used as test fixtures. |
| Medium | Production would otherwise inherit development schema mutation and verbose SQL settings. | A production profile now requires explicit database settings, validates the schema, caches Thymeleaf templates, suppresses SQL and public stack traces, and sets secure session-cookie attributes. |
| Medium | Hibernate reports the local MySQL-compatible server as version `5.5.5`, below its reported supported baseline. | Verify the actual database product/version and compatibility before production. No database changes were made. |

## B. Fixes implemented

- Removed the fixed admin registration code. `LIFELINK_ADMIN_REGISTRATION_CODE` is the only way to enable admin self-registration; it has no source default. Login rotates the session ID and refuses unknown roles.
- Added missing donor-profile checks to eligibility, scheduling, and history routes. Invalid eligibility input is rejected before persistence. Admin donation status and inventory blood type are validated.
- Added `application-prod.properties`, `Dockerfile`, and `.dockerignore`. The container runs as a non-root user. Production database credentials have no local fallback, and Hibernate uses `validate` rather than altering the schema.
- Replaced an unbounded donor table scan in the test suite with a transactional fixture. Added regression coverage for disabled admin signup, session-ID rotation, missing donor profiles, and invalid donation-status transitions.
- Kept earlier report fixes: isolated section-specific filters and database-backed report data. Updated system, test, and deployment documentation.
- Created `.codex-backups/lifelink-pre-audit-20261028.zip` before source edits (77 files; excludes `target`). There is no Git branch or worktree because this project has no `.git` directory.

Files changed in this audit:

- `src/main/java/lifelink/controller/RegistrationController.java`
- `src/main/java/lifelink/controller/LoginController.java`
- `src/main/java/lifelink/controller/DonorController.java`
- `src/main/java/lifelink/controller/AdminController.java`
- `src/main/resources/application.properties`
- `src/main/resources/application-prod.properties`
- `src/main/resources/templates/register.html`
- `src/main/resources/templates/donors.html`
- `src/test/java/lifelink/PublicPagesAndAuthenticationTests.java`
- `README.md`, `Dockerfile`, `.dockerignore`
- `docs/Testing-Results.md`, `docs/LifeLink-System-Guide.md`, `docs/Production-Deployment.md`
- `docs/Audit-Report-2026-10-28.md`

## C. Testing results

- Maven offline `test-compile`: **BUILD SUCCESS**; Maven reported generated classes up to date.
- Node syntax check for `reports.js`: **PASS**.
- Node report-filter test: **1 passed**.
- A previous complete baseline suite on 22 October passed 11/11 tests. It predates the current audit fixes.
- Current full Maven package/test did not complete: its integration run hung on local MySQL table queries. A targeted run also stalled during donor-table metadata inspection. Both test processes were stopped without modifying database contents. A clean build could not remove locked `target` output; a skipped-test packaging attempt was blocked by an unavailable Maven plugin artifact/cache write access. No fresh distributable JAR was verified.
- HTTP checks recorded in the earlier test report showed public pages at 200 and protected unauthenticated routes redirecting to login. Those checks used an existing listener and do not verify this final compiled state.
- Docker image build/run was not tested because Docker is not installed.

See [Testing Results](Testing-Results.md) for command context and the earlier baseline test details.

## D. Remaining issues

1. Implement and test CSRF protection for all state-changing forms and endpoints before any public deployment.
2. Restore a responsive local database connection and rerun the complete test suite; verify the current controller/template changes against an authenticated browser session.
3. Confirm database product/version and schema compatibility. Prepare and review migrations before production; `prod` startup is expected to fail if schema validation finds differences.
4. Add health/readiness and operational logging/monitoring. The current app has no dedicated readiness endpoint.
5. Complete browser checks for responsive layouts, password toggles, uploads, forms, and report filters.
6. Report filters remain client-side and load the rendered data set; implement server-side pagination/filtering for larger data volumes.

## E. Deployment status

**Not deployed.** No hosting provider/account, production database, domain, TLS configuration, or secret-manager values were supplied or detected. Public deployment is also blocked by missing CSRF protection, the unverified database version/schema, and the stalled local database. Do not point the application at the development database.

When those gates are resolved, follow [Production Deployment](Production-Deployment.md) to build and configure the container. Verify the HTTPS URL, role protections, synthetic-account workflows, database persistence, logs, backups, and rollback before declaring release.

## F. Run and maintain LifeLink

Local run (JDK 17+, MySQL): create the `lifelink_db` database without dropping existing records, set `LIFELINK_DB_URL`, `LIFELINK_DB_USERNAME`, and `LIFELINK_DB_PASSWORD` if needed, then run `mvnw.cmd spring-boot:run` on Windows. Open `http://localhost:8080/`. Run `mvnw.cmd test` after the database is responsive.

Production uses `SPRING_PROFILES_ACTIVE=prod`, `PORT`, and required `LIFELINK_DB_URL`, `LIFELINK_DB_USERNAME`, and `LIFELINK_DB_PASSWORD`; admin signup remains off unless `LIFELINK_ADMIN_REGISTRATION_CODE` is securely provisioned. Keep values in the host secret manager and outside source control. Use HTTPS at the trusted platform proxy, provision database backups, and review logs and rollback procedures before each release.
