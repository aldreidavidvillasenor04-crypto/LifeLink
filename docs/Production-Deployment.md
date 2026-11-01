# LifeLink production deployment

**Status: prepared locally, not published.** The app has no connected GitHub repository, Railway account, production database, or public domain in this workspace. No public URL or donor data has been uploaded.

## What the project uses

- Java 17, Spring Boot 3.5, Spring MVC, Thymeleaf, and Maven.
- MySQL through Spring Data JPA and MySQL Connector/J.
- A multi-stage Dockerfile that builds the app and runs it as a non-root user.
- A `prod` Spring profile that reads database credentials from environment variables, uses `PORT`, requires HTTPS session cookies, hides server errors, and validates rather than mutates the schema.

The server-rendered pages use relative/Thymeleaf URLs; no public page requires a hard-coded localhost address. The existing database stays local unless you separately approve a protected migration.

## Preparation in this branch

- Added Spring Security CSRF checks without replacing LifeLink's custom login/session-role behavior. Thymeleaf `th:action` forms receive CSRF tokens automatically; logout is now a POST action.
- Added a database-backed `/health/ready` endpoint for the host's deployment health check. It returns only `UP` or `DOWN` and never includes connection details.
- Added Flyway for production schema creation. `V1__initial_schema.sql` contains `CREATE TABLE` statements only, with no rows, `INSERT`, or `DROP`. Production applies it to a new database before Hibernate validates the schema. Local development keeps its existing `ddl-auto=update` behavior and does not run Flyway by default.
- Tightened Git and Docker ignore rules for `.env` files, backups, database dumps, build output, and common credential files. The versioned Flyway migration is explicitly allowed.
- Rewrote the README with local setup and deployment steps.

The migration was generated from schema metadata only. It is suitable for creating the initial schema in a new MySQL database. It does not migrate users, donors, appointments, patient requests, uploaded documents, or other existing records.

## Hosting choice: Railway

Railway is the closest fit because it supports Dockerfile deployments and a MySQL service with private connection variables. New accounts receive a one-time $5 trial credit; the Free plan then costs $0/month and includes $1/month of usage credit. Hobby costs $5/month and includes $5 of monthly resource usage, with usage above that billed separately. The Free plan has limited CPU, RAM, and volume storage, so treat it as a demo tier and set a spending limit before importing data. Confirm current pricing before creating services. [Railway pricing](https://docs.railway.com/pricing/plans), [Railway MySQL](https://docs.railway.com/databases/mysql).

Render can run the Java app in Docker, but it does not provide managed MySQL; a MySQL server there requires you to operate a custom database service on persistent storage. Render's free web services sleep after 15 minutes without traffic and take about a minute to wake; its free Postgres expires after 30 days and is not compatible with this app without a database migration. [Render Docker services](https://render.com/docs/docker), [Render free services](https://render.com/docs/free), [Render MySQL](https://render.com/docs/deploy-mysql).

## 1. Prepare and upload the source to GitHub

The local Git repository is initialized on branch `main`; it has no commits or remote yet. In VS Code, open the `lifelink` project directory, then use its PowerShell terminal. The root directory is:

```powershell
cd "C:\Users\ASUS_TUFF\Downloads\lifelink\lifelink"
git add .
git status --short
git diff --cached --check
```

Before committing, check that the staged list contains source, documentation, and the schema migration only—no `.env`, `.codex-backups`, `target`, SQL dumps, or real donor/patient records. Then commit:

```powershell
git commit -m "Prepare LifeLink for deployment"
```

If Git says your identity is not configured, set your commit name and email in the VS Code terminal first. Use the email associated with your GitHub account, or your GitHub-provided no-reply address. These settings label commits; they do not sign you in to GitHub.

```powershell
git config --global user.name "Your Name"
git config --global user.email "your-email@example.com"
```

On GitHub, select **New repository**, create an empty **Private** repository named `lifelink`, and do not have GitHub add a README, license, or `.gitignore`. Then replace `<YOUR_GITHUB_NAME>` with your GitHub username and run:

```powershell
git remote add origin https://github.com/<YOUR_GITHUB_NAME>/lifelink.git
git push -u origin main
```

Do not paste GitHub tokens or passwords into chat or source files. If Git prompts for authentication, use GitHub's browser-based sign-in or its approved credential manager.

## 2. Create the Railway services

1. Sign in to Railway and create a project from the GitHub repository. Authorize GitHub access only to this repository if possible.
2. Railway should detect the root `Dockerfile`. The project root in GitHub is the repository root; no subdirectory setting is needed.
3. In the Railway project, choose **New → Database → MySQL**. Keep the database private and in the same project/environment as the app. Name the database service `MySQL`, or update the reference-variable service name below to match your chosen name.
4. In the app service's **Variables** tab, set:

   ```text
   SPRING_PROFILES_ACTIVE=prod
   LIFELINK_DB_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}
   LIFELINK_DB_USERNAME=${{MySQL.MYSQLUSER}}
   LIFELINK_DB_PASSWORD=${{MySQL.MYSQLPASSWORD}}
   ```

   These are Railway reference variables; add them through its variable editor so the values stay linked to the MySQL service. Do not define `PORT`; Railway supplies it and the production Spring profile binds to it. Do not use the local XAMPP address or an externally exposed database URL.

5. Set the app service's **Healthcheck Path** to `/health/ready`. It returns success only when the application can query the database.
6. Before exposing a public domain, confirm the deployment is healthy, Flyway has created all seven tables, and acceptance tests pass with synthetic records. Then, under the app service's **Networking** settings, generate a Railway domain. The app uses the host's HTTPS proxy and Secure session cookies.

Railway's config-as-code files are being deprecated for new services, so this project uses the dashboard's current service settings rather than adding a legacy `railway.json` file. Railway documents Dockerfile discovery, private MySQL variables, reference-variable syntax, and health checks at [Dockerfiles](https://docs.railway.com/builds/dockerfiles), [MySQL](https://docs.railway.com/databases/mysql), [variables](https://docs.railway.com/variables), and [health checks](https://docs.railway.com/deployments/healthchecks).

## 3. Database and existing records

The first production deployment is intended for a **new, empty** hosted database. Flyway applies the schema migration; Hibernate then checks it. Do not point `prod` at the XAMPP database: it is a development database running MariaDB, not the hosted MySQL service.

No existing records have been exported or uploaded. Donor names, contact details, eligibility answers, patient requests, and uploaded hospital documents are sensitive. Before moving any records, confirm the organization is authorized to place them with the selected provider and region, create and verify an encrypted backup, and use a new empty destination. Never commit a full SQL dump or send it through chat. Importing data into an already populated database can collide with IDs and must not be attempted until a tested migration plan is approved.

The schema migration preserves the app's current table names and foreign-key relationships. For a record migration, make a separate encrypted, **data-only** export after the new schema has been applied, test it against a disposable staging database, compare record counts without printing personal data, then import into the still-empty production tables using a protected database connection. Keep the original local database unchanged until the hosted data and login behavior have been verified. Delete temporary exports only after the approved retention/backup policy permits it.

Railway's MySQL backups and point-in-time recovery require explicit setup in its dashboard. Turn them on and test restoration before storing real records. [Railway backup guidance](https://docs.railway.com/databases/mysql).

## 4. Admin account setup

Admin self-registration is disabled unless `LIFELINK_ADMIN_REGISTRATION_CODE` is set. For initial provisioning, set a long random value as a Railway secret, create the first admin through the registration page, then remove the variable and redeploy. Do not put the code in `.env`, GitHub, a URL, or a public issue. Leave the variable unset afterward unless there is a specific approved need.

## 5. Verify before sharing the URL

Run the local checks first:

```powershell
.\mvnw.cmd test
```

In a staging deployment, verify:

1. `/health/ready` reports `UP`; database failures return `503` with no connection details.
2. Home, About, Features, Contact, Login, and Register load over HTTPS with CSS, JavaScript, and images.
3. A synthetic donor can register, log in, reach only donor pages, complete their profile, and log out.
4. A synthetic admin can log in and use only admin pages; verify no admin account can access donor-only data.
5. Missing or invalid CSRF tokens are rejected; form submissions and the file-upload form work with tokens.
6. Appointment scheduling, eligibility review, blood requests, inventory, donation history, and reports persist and render correctly.
7. Database backups restore to a separate staging database. No local filesystem upload is treated as persistent storage.

The local suite does not replace browser testing of the deployed URL. Report a feature as verified online only after testing it on that actual URL.

## Current blockers

- The local Git repository is initialized on `main` and the reviewed project files are staged, but no commit or remote exists. Git identity is not configured in this environment. GitHub CLI and Railway CLI are not installed, and no hosting account or production database was supplied. I therefore could not create the GitHub repository, provision cloud services, or obtain a public URL.
- No existing donor/patient data was moved. Decide whether the public demo should start with a blank database or whether a legally approved, secure data migration is required.
- Before real records are used, complete an organizational privacy/security review and verify the provider's contractual and legal suitability for the data. This deployment preparation does not certify regulatory compliance.
- The local production-profile database migration and Hibernate schema validation passed on a disposable schema, but the real HTTP smoke test could not bind a local Tomcat port because both installed JDKs failed to create a Windows loopback selector. Recheck the health and HTTPS endpoints after Railway deploys the Java 17 Docker image.
