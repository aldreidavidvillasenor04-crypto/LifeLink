# LifeLink Demonstration Runbook

This sequence demonstrates the current LifeLink workflows without entering real donor or patient details. Use a prepared local MySQL database and test accounts. The automated fixtures described in the testing record roll back and are not persistent demo accounts.

## Before the demonstration

1. Start MySQL and confirm `lifelink_db` is available.
2. Set local database connection variables if needed, then start the application with `.\mvnw.cmd spring-boot:run` from the project root.
3. Open `http://localhost:8080/` and confirm Home, About, Features, Contact, Login, and Register load.
4. Confirm a prepared donor account and admin account can sign in. Do not use the source-controlled Admin registration validation value for a public demo; have a prepared Admin account instead.
5. Use clearly synthetic names and contact data. Avoid live patient information.

## Demonstration sequence

| Step | Action | What to show |
| --- | --- | --- |
| 1 | Visit Home, About, Features, and Contact | Distinct public content and the main navigation |
| 2 | Register or sign in as a prepared donor | Required donor details, password visibility control, and role-based redirect |
| 3 | Open Donor Dashboard | Blood type, preliminary eligibility, upcoming appointment, donation count/history, and blood requests from database data |
| 4 | Submit a health assessment | The existing questionnaire and its preliminary result/reasons; explain that clinical eligibility is decided by authorized staff |
| 5 | Request an appointment | Hospital, date/time, and appointment status. Respect the 90-day interval validation if the donor has a completed donation |
| 6 | Sign out and sign in as Admin | Admin role receives the Admin Dashboard and not the Donor Dashboard |
| 7 | Open donor management and assessment review | Existing donor records, search/archive actions, and assessment status/reasons |
| 8 | Record a donation and update its status | The donation is associated with the selected donor; completing it applies inventory once |
| 9 | Open Reports | Actual donor, donation, eligibility, appointment, and inventory records; demonstrate a filter within one section, then a no-match result and Clear filters |
| 10 | Sign out and request a protected route | Session is invalidated and the visitor is redirected to Login |

## Backup plan

If the app host or database fails during the demonstration, do not enter sample output into the live system or describe it as real database data. Use the final presentation to explain the module flow and architecture, show any screenshots captured from a verified run, and state which live data could not be refreshed. Keep this runbook and the automated test results available for questions.

Before presenting, refresh the screenshots in the final deck from the current build. Screenshots supplied with earlier bug reports show earlier login/registration layouts and an error page; they should not be presented as the final interface.
