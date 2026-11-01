# LifeLink Testing Results

This record lists checks that were actually run against the existing LifeLink code and MySQL configuration. It separates automated test results from live HTTP checks; a page returning a redirect is not counted as an authenticated page render.

## Deployment-preparation verification — 28 October 2026

- `mvn -B package`: **BUILD SUCCESS**; 15 tests passed, 0 failures, 0 errors. The packaged Spring Boot jar includes `SecurityConfig`, `/health/ready`, and `V1__initial_schema.sql`.
- The integration suite covers public page content, registration followed by donor login, admin role routing, invalid credentials, logout, CSRF token rejection, automatic token rendering in login and donor upload forms, and the database-backed readiness response.
- Started the production profile against a new, empty disposable local schema. Flyway applied V1 and Hibernate initialized successfully with `ddl-auto=validate`. The test schema was removed afterward. No rows were exported and the existing `lifelink_db` was not modified.
- The actual production-profile HTTP smoke test could not finish: Tomcat on both installed JDK 21 and JDK 26 failed to create its Windows loopback selector (`Unable to establish loopback connection`). The MockMvc tests do pass, but a TCP health/login smoke check still needs to run on the hosting platform or a local JDK/container that can bind a listener.
- Docker is unavailable here, so the Docker image itself was not built. No public deployment or public URL exists yet.

## Last completed automated suite — 22 October 2026

Final run completed from the project root on 22 October 2026 using Maven 3.9.16 and the existing user Maven cache. The launcher environment did not expose the expected home directory, so the run specified it explicitly:

```powershell
& 'C:\Users\ASUS_TUFF\.m2\wrapper\dists\apache-maven-3.9.16\0daed3be3ebd1c706f0e69e8b07c6b73f5cc4ea3dfce72a8d0ec2e849ca2ddb0\bin\mvn.cmd' '-Duser.home=C:\Users\ASUS_TUFF' '-Dmaven.repo.local=C:\Users\ASUS_TUFF\.m2\repository' test
```

**Result: 11 tests passed, 0 failures, 0 errors, 0 skipped.** Spring Boot loaded the test application context and connected to the configured MySQL database. Database-writing integration tests use `@Transactional` so their fixture rows roll back after each test.

| Objective | Scenario exercised | Expected result | Actual result | Status |
| --- | --- | --- | --- | --- |
| Application startup | Load the Spring application context for tests | Context initializes | Context loaded | PASS |
| Public navigation | Request Home, About, Features, and Contact | Each route renders its own page | All four returned 200 with page-specific content | PASS |
| Admin module rendering | Request the admin dashboard and management/report routes with an Admin session | Pages render within the role-aware shell | All covered routes returned 200 | PASS |
| Donor registration and login | Register a donor, sign in by username and email, render donor pages, try an invalid password, and log out | Valid sign-in routes to Donor Dashboard; invalid sign-in shows an error; logout returns to login | Assertions passed | PASS |
| Admin role routing | Sign in an Admin user and request Admin and Donor routes | Admin receives Admin Dashboard access and is redirected away from Donor routes | Assertions passed | PASS |
| Legacy donor profile | Sign in a donor user without a donor profile, complete required details, then open dashboard | Incomplete profile is directed to completion; completed profile reaches dashboard | Assertions passed | PASS |
| Donor dashboard database data | Render dashboard with an eligibility assessment and completed donation | Score, waiting interval, and donation history appear | Actual persisted test rows rendered in the response | PASS |
| Donor dashboard empty/status edge cases | Render existing donor records and a future appointment with null status | Dashboard handles empty optional data and does not throw on null status | Existing donor rendered; null-status regression case returned 200 | PASS |
| Eligibility and donation operations | Submit an assessment and complete a donation through the Admin route | Existing rules determine the result; donation status and inventory flag persist | Assessment returned `ELIGIBLE` for the passing fixture; donation was completed and inventory marked applied | PASS |
| Reports module | Render reports after inserting donor, donation, assessment, appointment, and inventory records | Report displays database-backed fields and stays Admin-only | Record values rendered; Donor session redirected to login | PASS |

## Earlier audit attempt — 28 October 2026

This was an incomplete diagnostic run before the successful deployment-preparation package run above. Its stalled tests and packaging attempts were not counted as passes; the later `mvn -B package` result is the final automated result recorded for this deployment preparation.

- `mvn -o test-compile` completed with `BUILD SUCCESS`. Maven reported Java sources up to date; generated class files were already newer than source files.
- A full `mvn package` run reached the Spring/MySQL integration tests but did not finish. The test process stalled on a read of all donor records. A separate read-only MySQL process-list check showed the query in `Statistics` for over 12 minutes and a donor table count waiting in `Opening tables` for over 11 minutes. A targeted test run then stalled while Hibernate inspected the donor table. Both test processes were stopped; no process-list kill or database repair was performed.
- `mvn clean package` could not remove a locked `target` directory. A later skipped-test packaging attempt could not resolve/write a missing Maven plugin artifact in the user cache. A fresh distributable JAR was not verified.
- The donor dashboard rendering test was changed to insert one transactional fixture instead of scanning every real donor record. The new regression tests for closed admin registration, donor profile completion redirects, session-ID rotation, and invalid donation status compiled, but could not be fully run while the database is stalled.
- `node --check src/main/resources/static/js/reports.js` passed; the report-filter test passed **1/1**.
- The local MySQL-compatible server reported version `5.5.5` to Hibernate, below the version Hibernate 6.6 reports as supported. Confirm the actual server/version before production use.
- Docker is not installed in this environment, so the Docker image could not be built or run.

No database rows or schemas were manually reset or deleted during this audit. Existing processes on the local host and their long-running queries were left untouched.

## Frontend script check

The bundled Node.js runtime ran `node --check src/main/resources/static/js/reports.js` successfully. It also ran `node --test src/test/js/reports-filter.test.mjs`: **1 test passed**. The test evaluates the production filter script with two isolated report sections and verifies combined search/blood/status/date filters, no-match feedback, invalid date range handling, reset behavior, and that one section's filters do not hide rows in another.

This Node harness verifies filter logic, but it does not replace browser-level interaction, accessibility, or cross-browser checks. Those checks remain for the demonstration run before release.

## Live HTTP smoke check

With a process listening at `localhost:8080`, the following responses were observed:

| Route | Observed response | Interpretation |
| --- | --- | --- |
| `/`, `/about`, `/features`, `/contact`, `/login`, `/register` | 200 | Public pages were reachable |
| `/admin/reports` without a session | 302 to login | Admin route access is restricted |
| `/donor/dashboard` without a session | 302 to login | Donor route access is restricted |

The listener was not the process created by the final `spring-boot:run` attempt, so this smoke check is not evidence that the latest compiled controller changes were served over HTTP. A fresh Tomcat launch could not be confirmed: one attempt failed while Java initialized its Windows loopback selector with `SocketException: Invalid argument: connect`, and later attempts found port 8080 already occupied. Authenticated current-source behavior is covered by MockMvc tests instead.

## Defects fixed and remaining verification

- The Donor Dashboard appointment filter used `List.of(...).contains(status)`. That call throws when an upcoming appointment has a null status. It now compares against `CANCELLED` and `DENIED` with null-safe string comparisons; the null-status test passes.
- The Reports page previously shared one global blood/date/search filter across unrelated report tables. Each report now has isolated controls, plus applicable blood type, status, date, and search criteria. The page has no-match and invalid date-range feedback. Data rendering and authorization are covered by MockMvc; interactive browser filtering still needs a browser-level test.
- The Reports route reads from existing repositories and does not use sample data. The current donor schema has no registration timestamp; that field is not represented as if it existed.
- Report filter logic has a passing Node test; a real-browser visual/accessibility pass remains outstanding.

No Admin records were changed by the automated test fixtures. Test-created database rows were enclosed in transaction rollback.
